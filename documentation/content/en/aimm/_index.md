---

title: "Database Mapping Optimization"
linkTitle: "DB Mapping"
description: "Learning-based optimization of cross-model database mappings across relational, document, and graph databases."
weight: 40
----------

This section documents a framework for learning-based optimization of cross-model database mappings. The problem it addresses is that the same [conceptual data](../theoretical-background/schema-category.md) can be realized in several physical forms: as normalized relational tables, embedded MongoDB documents, graph structures, or hybrids of these. These choices can preserve the same application-level meaning while producing different query latencies, storage costs, redundancy, and migration effort.

A [mapping](../theoretical-background/mapping.md) describes how conceptual objects and relationships are represented in concrete database systems. For example, an order may be split across relational tables, embedded as a document with its items, or partly duplicated to improve locality.

The project treats mapping design as an optimization problem. Rather than manually choosing one fixed layout, it evaluates alternatives using workload measurements, learned latency models, and search. The current implementation focuses on PostgreSQL, MongoDB, and Neo4j, but the workflow is structured so that the optimization logic is not tied to one database model.

## What the framework does

At a high level, the workflow is:

1. Generate benchmark data for a supported schema family.
2. Load the generated data into PostgreSQL, MongoDB, and Neo4j.
3. Run workload queries and collect measured latencies and query plans.
4. Build training datasets from the measured runs.
5. Train database-specific latency models.
6. Use the learned models inside a Monte Carlo Tree Search (MCTS) optimizer.
7. Compare candidate mappings using predicted workload cost and, where configured, storage or migration-related costs.

The latency models make it possible to estimate the cost of candidate mappings without executing every query under every possible layout. MCTS then uses those estimates to explore a large space of mapping alternatives, such as moving a kind to another database or changing whether related data is referenced or embedded.

## Supported scope

The repository currently includes code for three database backends:

* PostgreSQL for relational layouts,
* MongoDB for document layouts,
* Neo4j for graph layouts.

It also includes schema-specific benchmark modules, data generators, loaders, query registries, plan extractors, latency-estimation components, and MCTS optimization code. The implementation should be understood as a research prototype: it demonstrates the optimization workflow, but it is not a complete production database advisor.

<!-- For the full theoretical background, see the [MODELS paper](...). -->
