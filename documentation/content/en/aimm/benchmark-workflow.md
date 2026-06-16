---
title: "Benchmark Workflow"
weight: 6
---

The benchmark workflow produces the measured query files used by latency-model
training and optimization. It starts from a schema id, materializes that schema
inside one database system, generates workload queries, measures their
latencies, captures execution plans, and stores the result as JSONL.

The short version is:

```bash
python -m scripts.pipeline.generate_data edbt-0
python -m scripts.pipeline.populate_db postgres/edbt-0
python -m scripts.pipeline.measure_queries postgres/edbt-0 --num-queries 1000 --num-runs 10
```

The full workflow is:

1. generate schema data,
2. populate a concrete database instance,
3. generate query instances from a registry,
4. execute each query several times,
5. capture a database plan,
6. save measurements and metadata in the cache.

## Inputs

The workflow is controlled by two related ids.

| ID | Shape | Used by |
| --- | --- | --- |
| Schema id | `{schema}-{scale}` | data generation |
| Database id | `{driver}/{schema}-{scale}` | population and measurement |

For example, `edbt-0` identifies generated EDBT data at scale `0`, while
`postgres/edbt-0` identifies the PostgreSQL database instance populated from
that data.

The supported drivers are `postgres`, `mongo`, and `neo4j`. The supported schema
families are `art`, `edbt`, and `tpch`.

## Step 1: Generate Data

Data generation is database-independent:

```bash
python -m scripts.pipeline.generate_data edbt-0
```

The script:

1. parses the schema id,
2. loads `dynamic/common/{schema}/data_generator.py`,
3. calls the module's `export()` factory,
4. runs the returned `DataGenerator`.

Generated files are written to:

```text
data/inputs/{schema_id}/
```

For example:

```text
data/inputs/edbt-0/person.csv
data/inputs/edbt-0/product.csv
data/inputs/tpch-2/orders.csv
```

Most inputs are CSV files. Some schemas may also produce JSONL files when a
document database loader needs already-shaped documents.

Generation is deterministic for a schema and scale. The generator seed is based
on the global seed, schema name, and scale, so repeated generation of the same
schema id should produce consistent benchmark data.

## Step 2: Populate a Database

Population turns generated files into a concrete database instance:

```bash
python -m scripts.pipeline.populate_db postgres/edbt-0
python -m scripts.pipeline.populate_db mongo/edbt-0
python -m scripts.pipeline.populate_db neo4j/edbt-0
```

The script:

1. parses the database id,
2. loads `dynamic/{driver}/{schema}/loader.py`,
3. creates a driver through `DriverProvider`,
4. resets the target database by default,
5. loads the generated files,
6. saves per-kind load timings.

By default, population clears the previous data for that database instance. Use
`--no-reset` only when you intentionally want to keep existing data:

```bash
python -m scripts.pipeline.populate_db postgres/edbt-0 --no-reset
```

Population timing output is saved as:

```text
data/cache/{driver}/{schema_id}/populate.json
```

Example:

```text
data/cache/postgres/edbt-0/populate.json
```

The timing file maps physical tables, collections, labels, or relationships to
load durations. Later optimization code can use these timings as part of a
storage or migration-cost signal.

## Driver-Specific Loading

Each database family has its own loading strategy.

| Driver | Loading strategy |
| --- | --- |
| PostgreSQL | Creates tables, constraints, and indexes, then loads CSV files with `COPY FROM STDIN`. |
| MongoDB | Creates indexes, builds documents from CSV/JSONL inputs, and inserts them with `insert_many`. |
| Neo4j | Creates constraints, then uses `LOAD CSV` from Neo4j's `/import` directory to create nodes and relationships. |

Neo4j is the most sensitive to where files are stored. The Docker Compose setup
mounts:

```text
./data/inputs:/import:ro
```

The Neo4j loaders then refer to generated files through paths such as:

```text
file:///edbt-0/person.csv
```

## Step 3: Generate Query Instances

Query generation happens inside measurement:

```bash
python -m scripts.pipeline.measure_queries postgres/edbt-0 --num-queries 1000 --num-runs 10
```

The script loads:

```text
dynamic/{driver}/{schema}/query_registry.py
```

The registry produces concrete query instances for the requested scale. It uses
the same id conventions as the rest of the system, so a generated query id looks
like:

```text
postgres/edbt-0/mcts-3:12
```

The `--num-queries` value is a target. The registry always generates at least
one query per available template, so the actual count can be higher than the
requested value when there are more templates than requested queries.

Useful measurement flags:

| Flag | Meaning |
| --- | --- |
| `--num-queries` | Target number of generated query instances. |
| `--num-runs` | Number of timed executions per query. |
| `--no-write` | Exclude insert, update, and delete templates. |
| `--no-cache` | Ignore previous measurements and start the cache from scratch. |

## Step 4: Measure and Explain Queries

For each generated query, `scripts.pipeline.measure_queries` calls a driver-specific plan
extractor. The extractor performs two related tasks:

1. execute the query `--num-runs` times and record wall-clock latency,
2. capture a profiled or executed plan after the timing runs.

The shared interface is `BasePlanExtractor` in
`src/latency_estimation/plan_extractor.py`. The implementations are:

| Driver | Implementation |
| --- | --- |
| PostgreSQL | `src/latency_estimation/postgres/plan_extractor.py` |
| MongoDB | `src/latency_estimation/mongo/plan_extractor.py` |
| Neo4j | `src/latency_estimation/neo4j/plan_extractor.py` |

The measurement script appends each successful measurement as soon as it
finishes. If a long run is interrupted, completed measurements remain in the
JSONL file and the next run can continue from the cache.

## Database-Specific Measurement

PostgreSQL uses:

```sql
EXPLAIN (ANALYZE, FORMAT JSON, BUFFERS, VERBOSE)
```

for profiled plans. Timed write queries run inside a transaction and are rolled
back.

MongoDB uses `explain` with `executionStats` for measured plans. For normal
prediction later, flat-model code can refresh query-planner-only plans without
executing the query. During measurement, MongoDB write queries are restored:
updates replace the original documents, deletes reinsert the deleted documents,
and inserts remove the inserted documents afterward.

Neo4j uses `PROFILE` for measured plans. Write queries are run in a transaction
and rolled back. Non-profile prediction later uses `EXPLAIN`.

The plan shapes are intentionally not forced into a single universal format at
measurement time. Each JSONL row stores the database-native plan shape, and the
feature extractors interpret those plans later.

## Step 5: Store the Measurement Cache

Measured query files are saved under:

```text
data/cache/{driver}/{schema_id}/measured-{num_queries}-{num_runs}.jsonl
```

If write queries are disabled, the suffix includes `-nowrite`:

```text
data/cache/{driver}/{schema_id}/measured-{num_queries}-{num_runs}-nowrite.jsonl
```

Examples:

```text
data/cache/postgres/edbt-0/measured-1000-10.jsonl
data/cache/mongo/tpch-2/measured-1000-5-nowrite.jsonl
```

The first JSONL row is a header with:

- `database_id`,
- `num_queries`,
- `num_runs`,
- `allow_write`,
- `global_stats`.

Each following row is one query measurement with:

- `id`,
- `label`,
- `is_write`,
- `content`,
- `plan`,
- `times`.

The `times` array stores every timed run. Dataset creation later decides how to
aggregate those values into a training label.

## Cache Behavior

By default, measurement reuses an existing cache file. When the file exists,
the script loads previous rows and measures only generated query ids that are
missing from the cache.

This behavior is useful for long benchmark runs:

- completed measurements survive interrupts,
- failed queries can be retried after a fix,
- increasing `--num-queries` can append new query instances.

Use `--no-cache` when the existing file should be ignored and recreated:

```bash
python -m scripts.pipeline.measure_queries postgres/edbt-0 --num-queries 1000 --num-runs 10 --no-cache
```

After all generated queries have measurements, the script normalizes cache order
to match the current registry order.

## One-Command Pipeline

The top-level shell helper combines generation, population, and measurement:

```bash
./scripts/measure_queries_pipe.sh postgres edbt-0 1000 10
```

The arguments are:

```text
./scripts/measure_queries_pipe.sh <driver> <schema-id> <num-queries> <num-runs>
```

The helper is convenient for local runs and simple experiments. For larger
experiments, running each Python step separately makes it easier to reuse
generated data, avoid unnecessary repopulation, or resume measurement from a
cache.

## Run Counts and Warmup

Measurements collect multiple timings because database latency is noisy. The
first few executions can include effects from connection setup, cold caches,
query compilation, page cache state, or storage warmup.

The measurement file keeps all recorded timings. Downstream dataset creation
chooses how many early values to skip before averaging:

- the generic neural dataset path defaults to skipping the first `5` timings,
- the PostgreSQL flat dataset path also defaults to skipping the first `5`,
- the MongoDB and Neo4j flat dataset paths default to skipping the first `1`.

For training-oriented measurements, choose `--num-runs` high enough that
skipping early runs still leaves enough values to average. For quick smoke
tests, smaller values are fine as long as downstream dataset commands are not
asked to skip more timings than were collected.

## Artifact Summary

| Artifact | Path | Created by |
| --- | --- | --- |
| Generated input files | `data/inputs/{schema_id}/` | `scripts.pipeline.generate_data` |
| Populate timings | `data/cache/{driver}/{schema_id}/populate.json` | `scripts.pipeline.populate_db` |
| Measured query cache | `data/cache/{driver}/{schema_id}/measured-*.jsonl` | `scripts.pipeline.measure_queries` |

These are benchmark artifacts. Model datasets, feature extractors, trained
models, and evaluation plots are created by later latency-estimation steps.
