---
title: "Latency Models"
weight: 7
---

Latency models learn to predict query runtime from database plans and related
features. In this repository, the measured JSONL files from the benchmark
workflow are the raw input; dataset scripts turn those measurements into model
features and training labels.

The target value is query latency in milliseconds. The model input is not the
query text alone, but the database's view of how the query will run: operators,
estimated rows or costs, structural plan features, and, for some drivers,
database statistics.

## Why Prediction Is Difficult

Query latency is hard to predict because plans are only approximations of what
will happen at runtime.

The most important uncertainty is cardinality estimation. If the database
planner underestimates how many rows or documents a filter will return, a plan
can look cheap even though the query scans much more data than expected. That
error flows directly into the model features because estimated rows, costs, and
operator choices are part of the input.

## Model Paths

The repository has two latency-model paths.

| Path | Dataset scripts | Training scripts | Main use |
| --- | --- | --- | --- |
| Plan-structured neural models | `scripts.neural.create_dataset` | `scripts.neural.train` | QPP-Net-style experiments over plan trees. |
| Flat tree models | `scripts.flat.<driver>.create_dataset` | `scripts.flat.<driver>.train` | EXPLAIN-only prediction and MCTS integration. |

Both paths start from measured query files. They differ in how much of the plan
structure they preserve.

## Neural Plan Models

This model is an implementation of the [Plan-Structured Deep Neural Network](https://arxiv.org/abs/1902.00132) paper - feel free to refer to it for more details. 

The neural path converts each measured plan into a tree of `PlanNode` objects.
Each node stores:

- an operator type,
- a numeric feature vector,
- optional extracted operator latency,
- child plan nodes.

The feature extractor first scans training plans to build vocabularies and
normalization statistics. Then it converts each database-native plan into a
driver-specific tree representation.

Create a neural train/validation split:

```bash
python -m scripts.neural.create_dataset \
  postgres/edbt-demo-train \
  edbt-0/measured-100-6.jsonl \
  --val-dataset postgres/edbt-demo-val \
  --val-ratio 0.2 \
  --split-seed 42
```

This writes:

```text
data/cache/postgres/edbt-demo-train/dataset.pkl
data/cache/postgres/edbt-demo-train/feature_extractor.pkl
data/cache/postgres/edbt-demo-train/operators.json
```

`operators.json` records the operator signatures seen during dataset creation.
For neural models, an operator signature is the combination of operator type,
number of children, and feature dimension. The model creates one neural unit for
each signature found in the training set. Note that this is a latency-model term and is
not the same as a schema-category [signature]({{% relref "/theoretical-background/schema-category" %}}#morphisms),
which identifies morphisms in the conceptual model.

Train a neural model:

```bash
python -m scripts.neural.train \
  postgres/edbt-demo-model \
  edbt-demo-train \
  edbt-demo-val
```

Training checkpoints are saved under:

```text
data/checkpoints/{driver}/{model-name}/
```

Important checkpoint names are:

```text
best.pt
best_metrics.json
epoch/0005.pt
epoch/0005_metrics.json
```

Evaluate a neural checkpoint:

```bash
python -m scripts.neural.test \
  postgres/edbt-demo-model/best \
  edbt-demo-val
```

Evaluation writes:

```text
data/plots/evaluation_results.json
data/plots/evaluation_plots.png
```

Validation or test items with unseen operator signatures may be pruned because
the trained neural model has no unit for those operators.

## Flat Tree Models

Flat models convert each whole plan into one fixed-length numeric vector. They
are trained with tree regressors such as random forests and XGBoost.

Create a PostgreSQL flat dataset:

```bash
python -m scripts.flat.postgres.create_dataset \
  postgres/edbt-demo-flat-train \
  edbt-0/measured-100-6.jsonl \
  --val-dataset postgres/edbt-demo-flat-val \
  --val-ratio 0.2 \
  --split-seed 42
```

Flat artifacts are:

```text
data/cache/{driver}/{dataset-name}/flat_dataset.pkl
data/cache/{driver}/{dataset-name}/flat_feature_extractor.pkl
```

Train and evaluate a PostgreSQL flat model:

```bash
python -m scripts.flat.postgres.train \
  postgres/edbt-demo-flat-rf \
  edbt-demo-flat-train \
  edbt-demo-flat-val \
  --model-type random_forest

python -m scripts.flat.postgres.test \
  postgres/edbt-demo-flat-rf \
  edbt-demo-flat-val
```

Flat model artifacts are:

```text
data/checkpoints/{driver}/{model-name}/flat_model.pkl
data/checkpoints/{driver}/{model-name}/flat_metrics.json
```

The equivalent MongoDB and Neo4j commands are:

```text
scripts.flat.mongo.create_dataset
scripts.flat.mongo.train
scripts.flat.mongo.test

scripts.flat.neo4j.create_dataset
scripts.flat.neo4j.train
scripts.flat.neo4j.test
```

MongoDB and Neo4j flat trainers support additional tree families such as
`extra_trees` and `decision_tree`. PostgreSQL currently supports
`random_forest` and `xgboost`.

## Feature Extractors

Feature extractors are saved with datasets and models because they define the
feature contract. A test dataset should reuse the training feature extractor
when it needs the same vocabulary or feature ordering:

```bash
python -m scripts.flat.postgres.create_dataset \
  postgres/edbt-demo-flat-test \
  edbt-1/measured-1000-10.jsonl \
  --feature-extractor-dataset edbt-demo-flat-train
```

At a high level, the feature families are:

| Feature family | Examples |
| --- | --- |
| Plan structure | node count, tree depth, leaf count, parent-child operator patterns |
| Operator mix | scan, join, sort, aggregate, update, delete, traversal operators |
| Optimizer estimates | estimated rows, estimated cost, plan width, root and aggregate statistics |
| Query shape | filters, limits, sorts, grouping, expression counts, relationship patterns |
| Database statistics | MongoDB collection and field-distribution stats; schema-specific optional identifiers |

PostgreSQL flat features use plain `EXPLAIN` information such as estimated
costs, estimated rows, plan width, structural counts, and categorical operator
counts.

MongoDB flat features use `queryPlanner`-safe plans plus global collection and
field-distribution statistics. They also preserve original query filters on the
plan so selectivity-style features can be estimated without executing the query.

Neo4j flat features use plain `EXPLAIN` plan fields such as estimated rows,
operator structure, identifiers, and parsed `Details` patterns. They avoid
runtime counters from `PROFILE`.

## Training Labels

The training label is derived from the measured `times` array for each query.
Dataset creation skips early timing runs and averages the remaining values.

The defaults differ by path:

| Dataset path | Default `--skip-first` |
| --- | --- |
| Generic neural dataset | `5` |
| PostgreSQL flat dataset | `5` |
| MongoDB flat dataset | `1` |
| Neo4j flat dataset | `1` |

If a measurement file has too few runs for the requested skip count, dataset
creation may fail or fall back depending on the driver-specific script. For
training runs, collect enough timings that the label is still based on multiple
post-warmup values.

## Evaluation Metrics

Evaluation compares predicted latency with measured latency.

Common outputs include:

- predicted latency,
- measured latency,
- absolute error,
- mean and median absolute error,
- mean relative error,
- median and mean R-value,
- fraction of predictions within R-value thresholds such as `1.5`, `2.0`, or
  `5.0`.

The R-value is symmetric: it is the larger of `predicted / measured` and
`measured / predicted`. A value of `1.0` is perfect. A value of `2.0` means the
prediction is off by a factor of two in either direction.

Flat evaluation scripts write JSON results under `data/plots/` by default:

```text
data/plots/flat_evaluation_results.json
data/plots/mongo_flat_evaluation_results.json
data/plots/neo4j_flat_evaluation_results.json
```

Each result row includes the query id, label, predicted latency, measured
latency, and absolute error. MongoDB evaluation can also print verbose
per-template summaries with `--verbose`.

## Single-Query Prediction

Flat models can predict a single query without executing it.

PostgreSQL:

```bash
python -m scripts.flat.postgres.predict \
  postgres/edbt-demo-flat-rf \
  postgres/edbt-0 \
  "SELECT * FROM person LIMIT 10"
```

MongoDB:

```bash
python -m scripts.flat.mongo.predict \
  mongo/tpch-2-flat-rf \
  mongo/tpch-2 \
  '{"find":"orders","filter":{"o_orderkey":1}}'
```

Neo4j:

```bash
python -m scripts.flat.neo4j.predict \
  neo4j/tpch-2-flat-rf \
  neo4j/tpch-2 \
  "MATCH (n) RETURN n LIMIT 10"
```

PostgreSQL uses `EXPLAIN` without `ANALYZE`, MongoDB uses explain verbosity
`queryPlanner`, and Neo4j uses `EXPLAIN`. MongoDB prediction also needs cached
global stats with field distributions; those are normally produced while
creating MongoDB flat datasets.

## Which Model Type to Use

Use the neural model when predicting for PostgreSQL, as the shapes of its query
 plan are a good fit for it, unlike the shapes of MongoDB and Neo4j query plans. 

Use the flat (tree-based) model when predicting any of the PostgreSQL, MongoDB, Neo4j queries.
