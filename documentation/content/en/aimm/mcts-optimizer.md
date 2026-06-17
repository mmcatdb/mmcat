---
title: "MCTS Optimizer"
weight: 8
---

The MCTS optimizer uses trained latency models to search over workload
assignments. Instead of asking which database is best in general, it asks which 
database should serve each workload query when latency and storage cost are
considered together.

This page starts after latency models already exist. The optimizer can receive
latency through a callback, or through a precomputed query/database latency
matrix. Storage cost is still optional and callback-based.

## Why MCTS

A workload assignment grows combinatorially. If there are `N` workload queries
and `M` feasible databases, a simple query-to-database assignment space can have
up to `M^N` states.

The decisions also interact. Sending one query to MongoDB may look best for
latency, but sending several related queries there may require storing the same
physical collections or documents. Splitting related queries across systems can
reduce latency for individual queries while increasing storage cost.

Monte Carlo Tree Search is useful here because it does not require exhaustive
enumeration. It incrementally explores promising local changes while still
occasionally trying less visited alternatives.

## Core Objects

The generic optimizer lives in:

```text
src/search/mcts.py
```

The main data objects are:

| Object | Meaning |
| --- | --- |
| `WorkloadQuery` | A logical query with an id, weight, optional payload, feasible database ids, and storage item ids. |
| `DatabaseInstance` | A candidate database instance. |
| `PrecomputedLatencyEstimator` | A validated lookup table for query/database latency estimates. |
| `State` | A tuple of database ids, one for each workload query. |
| `ReassignQuery` | A local action that moves one query from one database to another. |
| `OptimizationResult` | The initial assignment, best assignment, cost breakdown, iteration count, and search trace. |

The order of a state tuple follows the order of the workload query list. For
example, with three queries:

```text
("postgres/edbt-3", "mongo/edbt-3", "neo4j/edbt-3")
```

means the first query is assigned to PostgreSQL, the second to MongoDB, and the
third to Neo4j.

## State and Feasibility

Every state must assign every query exactly once. A query can be restricted to
specific databases with `feasible_database_ids`, or through a custom
`can_execute` callback.

If no initial assignment is provided, the optimizer uses the first feasible
database for each query. This initial state becomes the baseline for reporting
improvement and computing rewards.

The optimizer validates:

- query ids are unique,
- database ids are unique,
- query weights are finite and non-negative,
- every query has at least one feasible database,
- cost estimates are finite and non-negative.

## Actions

An action is a single reassignment:

```text
move query q from database A to database B
```

In code, this is `ReassignQuery`. The optimizer only generates actions that
move a query to a feasible database. Applying one action creates a neighboring
state.

This local-action design keeps the branching factor manageable. Large changes
are represented as a sequence of small reassignments.

## Cost Function

The optimizer computes a raw latency component and a raw storage component.
Then it combines them with user-supplied weights:

```text
total_cost =
  latency_cost_weight * latency_cost
  + storage_cost_weight * storage_cost
```

Latency cost is query-weighted:

```text
latency_cost = sum(query.weight * predicted_latency(query, assigned_database))
```

Storage cost is charged once per physical storage item per assigned database.
If two queries assigned to the same database need the same storage item, that
item is counted once. If related queries are split across databases, the
required physical items can be counted separately for each database.

This is the mechanism that lets the optimizer trade off fast per-query
placements against storage duplication.

## Search Loop

Each MCTS iteration follows the usual phases.

Selection walks from the root through already-expanded children using a UCT-like
score:

```text
average_reward + exploration_constant * exploration_term
```

Expansion chooses one unexpanded admissible reassignment and creates the child
state.

Evaluation computes the state's weighted cost by calling the latency and
storage estimators. The reward is based on the initial baseline cost divided by
the current cost, so lower cost gives higher reward.

Backpropagation adds the reward to every node on the selected path and updates
edge visit counts.

The optimizer caches latency estimates, storage estimates, state costs, and
state rewards. This matters because the same state can be reached through
different reassignment orders. When `latency_estimates` is passed to
`MCTSOptimizer`, the optimizer validates that every feasible query/database pair
has a finite, non-negative precomputed latency before the search starts.

## EDBT Integration

The concrete EDBT runner is:

```text
src/scripts/run_mcts_edbt.py
```

It builds semantic query bundles from the EDBT `mcts-*` templates. Each bundle
represents one logical workload query and contains driver-specific versions of
that query:

- SQL for PostgreSQL,
- a `MongoQuery` for MongoDB,
- Cypher for Neo4j.

The current template set is:

```text
mcts-0 ... mcts-16
```

The runner creates three candidate database instances for the selected scale:

```text
postgres/edbt-{scale}
mongo/edbt-{scale}
neo4j/edbt-{scale}
```

The runner supports two latency workflows:

- live flat-model prediction, where each candidate latency is estimated by
  loading the driver-specific flat model, fetching a non-executing plan, and
  predicting from that plan,
- offline MCTS, where the runner loads a precomputed latency matrix and never
  connects to the database instances during search.

## Storage Model

The EDBT runner uses a lightweight storage-cost model based on expected record
counts from the EDBT data generator and configurable per-driver multipliers.

Storage items are namespaced by driver:

```text
postgres:order
mongo:order
neo4j:Order
neo4j:HAS_ITEM
```

When a query is assigned to a database, only storage ids matching that
database's driver contribute to the storage cost. This lets one semantic query
refer to the relevant physical tables, collections, labels, or relationships
for each candidate database system.

The default storage multipliers are defined in `run_mcts_edbt.py` and can be
overridden from the command line.

## Running the EDBT Optimizer

### Live Prediction

Before running the EDBT optimizer, the selected scale should be populated in all
three databases, and the flat models referenced by the command should exist.
MongoDB also needs cached global field statistics for prediction; these are
usually created during MongoDB flat dataset creation. If they are missing, the
runner can collect them with `--collect-mongo-global-stats`.

Example:

```bash
python -m scripts.mcts.run_edbt \
  --scale 3 \
  --iterations 20000 \
  --instances-per-template 1 \
  --postgres-model-id postgres/edbt-2-3-flat-rf \
  --mongo-model-id mongo/tpch-2-flat-xgb-log \
  --neo4j-model-id neo4j/tpch-2-flat-rf \
  --latency-cost-weight 1 \
  --storage-cost-weight 1
```

Useful flags:

| Flag | Meaning |
| --- | --- |
| `--scale` | EDBT scale used for query generation and database ids. |
| `--iterations` | Number of MCTS iterations. |
| `--instances-per-template` | Number of parameter instances per `mcts-*` template. |
| `--seed` | Random seed for reproducible search choices. |
| `--latency-cost-weight` | Weight applied to the latency component. |
| `--storage-cost-weight` | Weight applied to the storage component. |
| `--postgres-model-id` | PostgreSQL flat model id. |
| `--mongo-model-id` | MongoDB flat model id. |
| `--neo4j-model-id` | Neo4j flat model id. |
| `--collect-mongo-global-stats` | Collect MongoDB field stats if no cache exists. |
| `--describe-only` | Print setup information without running MCTS. |

Use `--describe-only` to inspect the workload, model ids, storage multipliers,
and full-union storage baselines before paying the cost of prediction and
search:

```bash
python -m scripts.mcts.run_edbt --scale 3 --describe-only
```

### Precomputed Latencies

For database-free MCTS runs, first precompute the latency matrix:

```bash
python -m scripts.mcts.precompute_edbt_latencies \
  --scale 3 \
  --instances-per-template 1 \
  --postgres-model-id postgres/edbt-2-3-flat-rf \
  --mongo-model-id mongo/tpch-2-flat-xgb-log \
  --neo4j-model-id neo4j/tpch-2-flat-rf
```

By default, this writes:

```text
data/cache/mcts/edbt-3/latency-estimates-1.jsonl
```

Use `--output` to choose a different path. The precompute step uses the same
flat-model prediction path as live MCTS, so it may need database access to fetch
plans. Once the file exists, MCTS can run without database access:

```bash
python -m scripts.mcts.run_edbt \
  --scale 3 \
  --iterations 20000 \
  --instances-per-template 1 \
  --latency-estimates data/cache/mcts/edbt-3/latency-estimates-1.jsonl \
  --latency-cost-weight 1 \
  --storage-cost-weight 1
```

When `--latency-estimates` is provided, `run_mcts_edbt` ignores the model-id
flags for the MCTS run. It still builds the same semantic workload and storage
cost model, validates that the matrix matches the selected scale and instance
count, and checks that every query/database pair has exactly one valid latency.

The latency matrix is JSONL. The first row is a header with:

- `format`,
- `format_version`,
- `schema`,
- `scale`,
- `instances_per_template`,
- `query_ids`,
- `database_ids`,
- `latency_unit`,
- `source_metadata`.

Each following row contains:

- `query_id`,
- `database_id`,
- `latency_ms`,
- optional `source_query_id`.

## Reading the Output

The setup section prints:

- scale,
- number of templates,
- instances per template,
- workload query count,
- iteration count,
- latency and storage weights,
- model ids or the latency-estimates path,
- storage multipliers,
- full-union storage baseline by database,
- semantic workload titles and weights.

The result section prints:

- iterations completed,
- number of unique states visited,
- initial weighted cost,
- best weighted cost,
- latency and storage breakdowns,
- best reward.

The best assignment section maps every semantic workload query to a database:

```text
mcts-3:0 weight=1: mongo/edbt-3 (12.34 ms, storage=order, product)
```

For each query, the runner also prints predicted latency on every candidate
database. The selected database is marked with `*`.

The final section lists stored physical items by database and their storage
cost. This is the easiest place to see whether the optimizer concentrated
related workload pieces in one system or spread them across systems for latency.

## Generic Example

There is also a synthetic example that does not require trained database
models:

```bash
python -m scripts.mcts.run_example --iterations 20000 --storage-cost-weight 1
```

It uses generated latency and storage callbacks to demonstrate the optimizer's
behavior. Use it for understanding the generic search mechanics; use
`run_mcts_edbt` for the repository's real cross-database EDBT workflow.
