---
title: "System Architecture"
weight: 5
---

The repository is organized around one repeated idea: the same conceptual
benchmark can be materialized for different database systems, measured with
database-specific plan extractors, and then converted into latency-model inputs.

At a high level, the system has four layers:

1. shared core abstractions in `src/core/`
2. schema and driver-specific implementations in `dynamic/`
3. latency-estimation code in `src/latency_estimation/`
4. optimization code in `src/search/` and experiment scripts

The command-line scripts in `src/scripts/` connect these layers into runnable
workflows.

## Repository Map

| Area | Purpose |
| --- | --- |
| `src/core/` | Shared configuration, database drivers, id parsing, query abstractions, data-generator base classes, and loader base classes. |
| `dynamic/` | Schema-specific data generators, loaders, and query registries. |
| `src/scripts/` | Python entry points run with `python -m scripts.<name>`. |
| `scripts/` | Top-level shell helpers that chain Python scripts for common workflows. |
| `src/latency_estimation/` | Plan extractors, feature extractors, datasets, models, trainers, evaluators, and flat-model implementations. |
| `src/search/` | MCTS optimizer and workload-routing search structures. |
| `src/experiments/` | Experiment helpers and plotting utilities. |
| `data/inputs/` | Generated import files. |
| `data/cache/` | Populate timings, measured query JSONL files, datasets, and feature extractors. |
| `data/checkpoints/` | Trained model artifacts. |
| `data/plots/` | Evaluation outputs and plots. |
| `data/experiments/` | Experiment outputs. |

The Python package uses `src` layout. After `pip install -e .`, modules under
`src/scripts/` are available as `python -m scripts.generate_data`,
`python -m scripts.measure_queries`, and similar commands.

## Drivers

The supported database drivers are defined in `src/core/drivers.py`.

| Driver id | Driver class | Query representation |
| --- | --- | --- |
| `postgres` | `PostgresDriver` | SQL string |
| `mongo` | `MongoDriver` | `MongoQuery` object |
| `neo4j` | `Neo4jDriver` | Cypher string |

`src/core/driver_provider.py` creates and reuses driver instances from the
configuration loaded by `src/core/config.py`.

PostgreSQL and MongoDB use a database name derived from the schema id, such as
`edbt_0` for `edbt-0`. PostgreSQL databases are created on demand before
population. Neo4j Community Edition is handled differently: it uses separate
server ports instead of separate logical databases. `NEO4J_PORTS` can map a
schema name or schema id to a specific Neo4j instance; otherwise the default
Neo4j port is used.

## Schema Families

The implemented schema families are:

- `tpch`
- `edbt`
- `art`

A schema id combines the family with a scale:

```text
{schema}-{scale}
```

Examples are `tpch-2`, `edbt-0`, and `art-3`.

The scale is passed to data generators and query registries. For the generated
schemas, increasing scale usually increases the amount of data rapidly, so scale
`0` is the practical choice for local checks.

## Dynamic Modules

The `dynamic/` directory is where schema-specific behavior lives. Shared schema
logic is placed under `dynamic/common/{schema}/`, while database-specific logic
is placed under `dynamic/{driver}/{schema}/`.

For example:

```text
dynamic/common/edbt/data_generator.py
dynamic/postgres/edbt/loader.py
dynamic/postgres/edbt/query_registry.py
dynamic/mongo/edbt/loader.py
dynamic/mongo/edbt/query_registry.py
dynamic/neo4j/edbt/loader.py
dynamic/neo4j/edbt/query_registry.py
```

Dynamic modules are loaded by `src/core/dynamic_provider.py`. Each module
exports its implementation through an `export()` factory. The loader selects the
right file from the requested class, driver, and schema:

| Requested class | Dynamic file |
| --- | --- |
| `DataGenerator` | `dynamic/common/{schema}/data_generator.py` |
| `BaseLoader` | `dynamic/{driver}/{schema}/loader.py` |
| `QueryRegistry` | `dynamic/{driver}/{schema}/query_registry.py` |

This is why one command shape can work across many combinations:

```bash
python -m scripts.generate_data edbt-0
python -m scripts.populate_db mongo/edbt-0
python -m scripts.measure_queries neo4j/tpch-2 --num-queries 1000 --num-runs 10
```

## Data Generation

Data generators inherit from `src/core/data_generator.py`. They receive a schema
name, a scale, and an import directory. They produce files under:

```text
data/inputs/{schema_id}/
```

Most generated files are CSV files. Some MongoDB loaders can also consume JSONL
files when a schema needs prebuilt document structures.

Data generation is deterministic for a schema and scale. The seed is derived
from `GLOBAL_RNG_SEED`, the schema name, and the scale, so rerunning a generator
for the same schema id should produce consistent input data.

## Database Loading

Loaders inherit from `src/core/loaders/base_loader.py` through a driver-specific
base loader:

| Driver | Base loader | Main loading behavior |
| --- | --- | --- |
| PostgreSQL | `PostgresLoader` | Creates tables, constraints, and indexes, then loads CSV files with `COPY FROM STDIN`. |
| MongoDB | `MongoLoader` | Creates indexes, builds document structures from CSV/JSONL inputs, and inserts documents with `insert_many`. |
| Neo4j | `Neo4jLoader` | Creates constraints and loads CSV files with `LOAD CSV` from Neo4j's mounted `/import` directory. |

The database-specific loaders in `dynamic/{driver}/{schema}/loader.py` define
the physical representation for that schema and driver. This is where the
relational tables, MongoDB document nesting, or Neo4j node and relationship
layout are chosen.

Population timings are saved as:

```text
data/cache/{driver}/{schema_id}/populate.json
```

## Queries and Workloads

Query registries live in:

```text
dynamic/{driver}/{schema}/query_registry.py
```

They extend `QueryRegistry` from `src/core/query/query_registry.py`. A registry
contains query templates and generates concrete `QueryInstance` objects for a
requested scale.

Each query template has:

- a template name,
- a human-readable title,
- a weight,
- a read/write flag,
- an optional maximum scale.

Query ids have the form:

```text
{driver}/{schema}-{scale}/{template}:{instance_index}
```

For example:

```text
postgres/edbt-0/mcts-3:12
```

The same conceptual template can have different concrete query text for each
driver. For PostgreSQL it is SQL, for MongoDB it is a structured `MongoQuery`,
and for Neo4j it is Cypher.

## Plan Extraction

Plan extractors implement `BasePlanExtractor` from
`src/latency_estimation/plan_extractor.py`. The script
`src/scripts/measure_queries.py` uses `get_plan_extractor()` to choose the
driver-specific implementation.

| Driver | Plan extractor |
| --- | --- |
| PostgreSQL | `src/latency_estimation/postgres/plan_extractor.py` |
| MongoDB | `src/latency_estimation/mongo/plan_extractor.py` |
| Neo4j | `src/latency_estimation/neo4j/plan_extractor.py` |

Each extractor has three responsibilities:

- execute a query repeatedly and measure wall-clock latency,
- capture the database execution plan,
- collect any global database statistics used by later feature extraction.

The measurement details differ by database, but the stored output has one
common shape: a measured query file containing metadata, query contents, plans,
and timing values.

## Latency Estimation

Latency-estimation code is split between shared interfaces and driver-specific
implementations.

Shared files such as `dataset.py`, `feature_extractor.py`, `model.py`,
`trainer.py`, and `model_evaluator.py` define the general neural dataset and
training path.

Driver-specific directories provide plan and feature handling:

```text
src/latency_estimation/postgres/
src/latency_estimation/mongo/
src/latency_estimation/neo4j/
```

There are two broad model paths:

- neural, plan-structured models created with `scripts.create_dataset` and
  trained with `scripts.train`,
- flat, fixed-length feature models created with driver-specific
  `scripts.create_*_flat_dataset` scripts and trained with driver-specific
  `scripts.train_*_flat` scripts.

Flat models are the path used by the current single-query prediction scripts
and the EDBT MCTS integration.

## Search and Optimization

The search code lives in `src/search/mcts.py`. It defines the workload-routing
MCTS optimizer, including:

- workload queries,
- database instances,
- query reassignment actions,
- latency and storage cost callbacks,
- optimization results and cost breakdowns.

The concrete EDBT integration is in `src/scripts/run_mcts_edbt.py`. It builds
driver-specific query bundles, loads trained flat models, predicts query
latencies from plans, estimates storage cost, and runs MCTS over candidate
assignments.

The optimizer page describes this process in detail. From an architecture point
of view, the important boundary is that MCTS depends on prediction callbacks and
database metadata, not on the benchmark measurement scripts directly.

## Adding a New Schema or Driver Mapping

Most changes for a new schema or physical mapping happen in `dynamic/`.

For a new schema family, add:

- `dynamic/common/{schema}/data_generator.py`,
- `dynamic/{driver}/{schema}/loader.py` for each supported driver,
- `dynamic/{driver}/{schema}/query_registry.py` for each supported driver.

For a new physical mapping of an existing schema, the usual change is to add or
modify the driver-specific loader and query registry. The shared command-line
scripts can then use the same id conventions as the existing mappings.
