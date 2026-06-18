---
title: "Optimization Problem"
weight: 4
---------

The [Motivation and Concepts](motivation-and-concepts.md) section introduced the main idea behind database mappings: the same [conceptual data](../theoretical-background/schema-category.md) can be represented in several valid ways across relational, document, and graph databases. This section describes how the project turns those mapping choices into an optimization problem.

The goal is not only to check whether a mapping is valid. The goal is to compare valid mappings with respect to a workload and choose mappings that are expected to work better under the current requirements.

## Candidate mappings

A candidate [mapping](../theoretical-background/mapping.md) describes how the kinds from the reduced schema are represented in the available database instances.

A [kind](../theoretical-background/mapping.md) is a unit of storage relevant to the optimizer, such as a table, collection, node, or relationship. For each kind, a candidate mapping may decide that the kind is:

* stored directly in a database, for example as a PostgreSQL table, MongoDB collection, or Neo4j node or relationship
* embedded into another kind, for example as a nested document structure
* represented redundantly in more than one database

A candidate mapping must still preserve the conceptual meaning of the schema. For example, if `order_item` connects an `order` to a `product`, the mapping must still represent that connection somehow. It may be represented as a relational foreign-key relationship, a nested document structure, a graph edge, or another supported representation, depending on the database and the allowed mapping choices.

In this sense, the optimizer searches over physical and logical representations of the same conceptual model. It does not change the application domain itself.

## Inputs to the optimization problem

The optimizer evaluates candidate mappings using several inputs.

### Schema

The schema provides the domain structure that must be preserved. Scalar attributes are usually not the main optimization units. They matter for query execution and storage size, but the optimizer primarily reasons about larger mapping decisions such as where a kind is stored or whether one kind is embedded into another.

![Reduced schema showing the storage kinds considered by the optimizer](/img/aimm/MODELS2026-reduced-schema.svg)

### Available database instances

The optimizer also receives the set of database instances that may be used as targets.

For example, a configuration may include:

* one PostgreSQL instance
* one MongoDB instance
* one Neo4j instance
* or several instances of the same database technology

The optimization is defined over concrete database instances, not just abstract database types. Two PostgreSQL instances, for example, may have different deployment settings, data sizes, indexes, hardware, or cost profiles. In documentation and examples, labels such as relational, document, and graph are useful shortcuts, but the optimizer reasons about the database instances that are actually available.

### Workload queries

The workload describes what the application needs to do with the data. A workload may contain queries such as:

* retrieving an order with its items
* looking up products
* traversing customer relationships
* reading data that depends on several related kinds

A mapping that is good for one workload may be poor for another. For example, embedding order items inside an order document may help when complete orders are read frequently. The same design may be less attractive if the embedded data is large, highly redundant, and/or the embedding isn't even utilized by the workload.

### Query weights

Each workload query can be assigned a weight. The weight describes how important the query is in the optimization objective.

The most common interpretation is query frequency. A query executed thousands of times per hour should usually matter more than a query executed once per day. However, weights can also represent user-defined importance. For example, a latency-sensitive checkout query may receive a higher weight than a background reporting query even if both appear in the workload.

### User restrictions

The search space can be restricted by user-defined constraints. These restrictions are useful when some mappings are technically possible but not acceptable for a particular deployment.

For example:

* a specific kind must be stored PostgreSQL
* a specific kind must not be stored in MongoDB
* a graph relationship must be stored in Neo4j

## Optimization objective

Each candidate mapping is evaluated using a cost function. Lower cost means a better mapping according to the selected objective.

At a high level, the objective combines workload latency with storage and migration-related costs:

```text
total cost =
    latency weight * predicted workload latency
  + storage weight * storage cost
```
  <!-- + migration weight × migration cost -->

The exact weights depend on the experiment or use case. If query speed is the main concern, the latency term can dominate. If storage overhead or data movement is more important, the storage or migration terms can receive more weight.

## Predicted workload latency

The latency part estimates how expensive the workload is under a candidate mapping.

For each query, the system checks whether the query can be executed using the database-specific configuration induced by the mapping. If the query can be executed in more than one database, the optimizer can compare the alternatives and use the cheapest feasible one for that query.

The weighted workload latency can be understood as:

```text
weighted latency =
  sum over workload queries:
    query weight × predicted query latency under the candidate mapping
```

The important point is that the optimizer should not execute every query on every possible mapping during search. Doing so would require repeatedly materializing many alternative database layouts, which quickly becomes impractical. Instead, the framework uses measured workload data and learned latency models to estimate how expensive a query is likely to be under a candidate mapping.

## Storage and migration cost

Latency is not the only relevant criterion. Some mappings improve read performance by duplicating or embedding data, but this may increase storage usage and make updates more expensive.

For example:

* embedding product snapshots into orders may make order reads cheaper
* storing the same conceptual data in multiple databases may improve query locality
* but both choices may increase redundancy and storage consumption

Migration cost captures the effort required to move from one mapping to another. A candidate mapping may look attractive because it improves query latency, but if reaching it requires moving large amounts of data or restructuring many kinds, the improvement may not be worth the migration cost.

## Why exhaustive enumeration is not practical

In principle, one could enumerate every valid mapping, evaluate each one, and choose the cheapest. This is usually not practical.

The search space grows exponentially because each kind may have several possible representations and placements. Kinds can also interact. Moving one kind may make a query faster only if another related kind is moved with it. Embedding may improve locality for one query while increasing redundancy for another. A choice that looks good locally may be poor once the whole workload is considered.

![Example mapping space with single-step and multi-step mapping changes](/img/aimm/MODELS2026-mapping_space.svg)

For this reason, the project treats mapping design as a search problem. Later sections describe how the implementation uses Monte Carlo Tree Search to explore this space efficiently without evaluating every candidate exhaustively.
