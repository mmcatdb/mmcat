---
title: "Motivation and Concepts"
weight: 3
---------

Multi-model applications often store related data across different database systems. The same application domain might contain relational tables, document collections, and graph structures at the same time. This is useful, but it also creates a design problem: the same [conceptual data](../theoretical-background/schema-category.md) can be represented in several valid ways, and those choices affect performance, redundancy, and migration effort.

This section introduces the main motivation behind the project and the core modeling concepts used throughout the rest of the documentation.

## Why mapping design matters

A database [mapping](../theoretical-background/mapping.md) describes how conceptual data is represented in concrete storage systems. For example, an application may have concepts such as customers, orders, order items, products, and social relationships between customers. These concepts can be preserved across different database layouts, but each layout will behave differently under a workload.

Some common trade-offs are:

| Design choice          | Typical advantage                                                            | Typical cost                                                                         |
| ---------------------- | ---------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| Embedded documents     | Better locality for aggregate reads, such as loading an order with its items | More redundancy and potentially more expensive updates                               |
| Normalized tables      | Less duplication and clearer update semantics                                | More joins or lookups for queries that need related data together                    |
| Graph structures       | Efficient representation of traversal-heavy relationships                    | Less suitable for workloads dominated by tabular aggregation or document-style reads |
| Cross-system placement | Allows each part of the domain to use a suitable database model              | Adds complexity when data must be migrated, synchronized, or queried together        |

There is usually no single best mapping. A document representation may be a good choice when the workload frequently retrieves complete aggregates. A normalized relational representation may be better when shared entities are updated often. A graph representation may be preferable when queries traverse links, such as customer-follow relationships or friends-of-friends patterns.

The project treats these choices as optimization decisions rather than fixed implementation details.

## Same meaning, different mappings

A central idea is that multiple mappings can preserve the same conceptual meaning.

For example, an order can be represented as:

* a relational `order` table linked to `order_item` and `product` tables,
* a MongoDB document with embedded order items and product snapshots,
* or a mixed representation where orders are stored as documents while products remain normalized elsewhere.

All of these mappings can represent the same domain-level idea: an order has items, and those items refer to products. What changes is not the application meaning, but the physical and logical representation used to store and query it.

![Same sample data represented as relational tables, graph nodes, and a document collection](/img/aimm/MODELS2026-sample-data.svg)

This distinction matters because the optimizer should not ask only whether a mapping is valid. It should ask whether a mapping is suitable for a given workload, cost model, and deployment environment.

## Conceptual schema

The conceptual schema is the stable, database-independent view of the domain. It describes what exists in the application model before deciding where or how it is stored.

For example, a conceptual schema may say that:

* an `order` has related `order_item` records
* an `order_item` refers to a `product`
* a `customer` can follow another `customer`
* entities have identifiers and attributes

This conceptual view is intentionally separate from concrete database structures. Whether `order_item` becomes a relational table, an embedded document array, or some other representation is a mapping decision.

![Conceptual schema with person, customer, product, order, and order item relationships](/img/aimm/MODELS2026-sample-conceptual-schema.svg)

## Mapping changes as migration steps

A mapping (i.e., an assignment of kinds to database instances) can change while the conceptual schema stays the same.

For example:

* a kind may move from PostgreSQL to MongoDB
* a Neo4j graph relationship may be represented as a relational table
* a collection in MongoDB may be stored as a node in Neo4j instead

The project treats these changes as transitions between mappings. This makes migration part of the optimization problem: a new mapping may improve query performance, but it may also require data movement, duplication, or restructuring.

![Alternative database mappings for the same conceptual entities](/img/aimm/MODELS2026-alternative_mapping.svg)

## What the optimizer does

The optimizer works over candidate mappings. Each candidate decides how kinds are represented and where they are placed. Candidate mappings are then evaluated by weighing: 
1. the expected latency of a provided query workload using the mapping 
2. the storage and migration cost of the mapping

In practice, this means the system can compare alternatives such as:

* keeping everything normalized in PostgreSQL
* storing social links in Neo4j
* or moving selected kinds between systems

As a final note, the overall goal isn't to replace database design judgment, but instead to make mapping alternatives explicit and measurable, so that database layouts can be selected with respect to an actual workload rather than chosen manually once and then left fixed.
