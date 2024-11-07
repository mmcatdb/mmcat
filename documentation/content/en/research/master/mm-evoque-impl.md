---
title: "Querying over multi-model data"
---

Most applications need some persistent storage. The most common way to store data is to use a relational database. However, in more complex use cases, it's much more efficient to store the data in different data models (e.g., graph, document, or key-value) and databases (e.g., Neo4j, MongoDB, or Redis). We want to develop a tool that would enable users to query all these databases in a unified way.

Our framework allows us to create a unified, database-independent model of the data and then map its parts to the specific databases. We have also developed MMQL, a SPARQL-like query language over the unified model, and implemented the most basic use cases for three databases (PostgreSQL, Neo4j, MongoDB). The goal of the project is to implement the rest, specifically:
- filtering,
- sorting,
- aggregations,
- optional statements,
- nested queries.

The project can be extended to a diploma thesis, where the focus will be on implementing query rewriting/updating. This means that when the unified model of the data changes, the tool will automatically update the queries to reflect these changes. There are several ways to do this, e.g., heuristics, machine learning, or formal methods.

### Literature

1. Pavel Koupil, Jáchym Bártík, and Irena Holubová. 2024. MM-evoque: Query Synchronisation in Multi-Model Databases. International Conference on Extending Database Technology. [http://doi.org/10.48786/edbt.2024.78](http://doi.org/10.48786/edbt.2024.78)
2. Pavel Koupil, Daniel Crha, and Irena Holubová. 2023. MM-quecat: A Tool for Unified Querying of Multi-Model Data. International Conference on Extending Database Technology. [https://doi.org/10.48786/edbt.2023.76](https://doi.org/10.48786/edbt.2023.76)
3. Pavel Koupil, and Irena Holubová. 2022. A unified representation and transformation of multi-model data using category theory. J Big Data 9, 61 (2022). [https://doi.org/10.1186/s40537-022-00613-3](https://doi.org/10.1186/s40537-022-00613-3)

**This project is currently available.**
