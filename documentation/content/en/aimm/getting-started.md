---
title: "Getting Started"
weight: 2
---

This page gets a fresh checkout to the first useful benchmark artifact: a
measured query file. It uses PostgreSQL with the small `edbt-0` schema instance
as a smoke test, but the same command shape is used for MongoDB, Neo4j, and the
other supported schema families.

After this page, you should have:

- local Python dependencies installed,
- local database containers running,
- generated input files under `data/inputs/edbt-0/`,
- a populated PostgreSQL database for `postgres/edbt-0`,
- a measured JSONL file under `data/cache/postgres/edbt-0/`.

The later pages explain the repository structure, the full benchmark workflow,
latency-model training, and optimization. This page intentionally stays on the
shortest path that proves the local setup works.

## Requirements

You need:

- Python 3 with `venv` and `pip`
- Docker with Docker Compose
- enough local disk space for generated data, measurement caches, and model
  artifacts

The Python package is installed in editable mode. That makes the modules under
`src/scripts/` available as commands such as:

```bash
python -m scripts.pipeline.generate_data edbt-0
```

## Python Setup

Create and activate a virtual environment:

```bash
python -m venv .venv
source .venv/bin/activate
```

Install the repository:

```bash
pip install -e .
```

The project dependencies are declared in `pyproject.toml`. They include the
database clients for PostgreSQL, MongoDB, and Neo4j, plus machine-learning
libraries used by the later latency-model pages.

## Configuration

Create a local environment file:

```bash
cp .env.sample .env
```

The sample configuration is set up for the local Docker Compose services:

| Service | Host port |
| --- | --- |
| PostgreSQL | `3500` |
| MongoDB | `3501` |
| Neo4j default instance | `3502` |
| Additional Neo4j instance for `art` | `3503` |

The repository also has configurable artifact roots. They have defaults in
`src/core/config.py`, so you only need to add them to `.env` if you want to
override the default locations:

```env
IMPORT_DIRECTORY=data/inputs
CACHE_DIRECTORY=data/cache
CHECKPOINTS_DIRECTORY=data/checkpoints
RESULTS_DIRECTORY=data/plots
EXPERIMENTS_DIRECTORY=data/experiments
DEVICE=cpu
```

## Start the Databases

Start the development database containers:

```bash
docker compose up -d
```

This starts PostgreSQL, MongoDB, and two Neo4j services using `compose.yaml`.
The Neo4j containers mount `./data/inputs` into `/import`, so Neo4j data should
be generated through the repository scripts before loading.

To stop the containers later:

```bash
docker compose down
```

## ID Conventions

Most scripts use compact ids instead of many separate arguments.

| ID type | Shape | Example |
| --- | --- | --- |
| Schema ID | `{schema}-{scale}` | `edbt-0`, `tpch-2` |
| Database ID | `{driver}/{schema}-{scale}` | `postgres/edbt-0` |
| Dataset ID | `{driver}/{dataset-name}` | `mongo/tpch-2-flat-train` |
| Model ID | `{driver}/{model-name}` | `neo4j/tpch-2-flat-rf` |
| Checkpoint ID | `{driver}/{model-name}/{checkpoint}` | `postgres/edbt-model/best` |

The driver is one of `postgres`, `mongo`, or `neo4j`. The schema family is one
of `art`, `edbt`, or `tpch`.

For generated schemas, scale controls the amount of data. Use scale `0` for
local setup checks. Larger scales can grow quickly.

## Smoke Test

Generate input files for a small EDBT dataset:

```bash
python -m scripts.pipeline.generate_data edbt-0
```

This writes import files under:

```text
data/inputs/edbt-0/
```

Load the generated data into PostgreSQL:

```bash
python -m scripts.pipeline.populate_db postgres/edbt-0
```

This creates or resets the target PostgreSQL database and records load timings
under:

```text
data/cache/postgres/edbt-0/populate.json
```

Measure a small set of generated queries:

```bash
python -m scripts.pipeline.measure_queries postgres/edbt-0 --num-queries 100 --num-runs 6
```

The measurement script loads the PostgreSQL EDBT query registry, generates
query instances, executes each query several times, stores measured latencies,
and saves the profiled PostgreSQL plan. The output is a JSONL cache file such
as:

```text
data/cache/postgres/edbt-0/measured-100-6.jsonl
```

For a one-command version of the same generate-populate-measure path, use the
top-level shell helper:

```bash
./scripts/measure_queries_pipe.sh postgres edbt-0 100 6
```

## Check the Result

The important output from this page is the measured JSONL file. It is the input
used later to build latency-model datasets.

The first row contains metadata such as the database id, number of queries,
number of runs, whether write queries were allowed, and any collected global
database statistics. Each following row is one measured query with:

- query id and label,
- whether the query is a write,
- query content,
- captured plan,
- measured latency values.

Dataset creation skips early timing runs by default, so keep at least several
`--num-runs` values when you intend to use the measurement file for training.
The smoke-test command above uses `6` runs for that reason.

## Where to Go Next

Read [system-architecture]({{% relref "/aimm/system-architecture" %}}) to understand where the drivers,
dynamic schema modules, scripts, latency-estimation code, and search code live.

Read [benchmark-workflow]({{% relref "/aimm/benchmark-workflow" %}}) for the full generate-populate-measure
workflow, including cache files, query registries, plan extraction, and
database-specific measurement behavior.
