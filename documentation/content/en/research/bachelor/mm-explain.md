---
title: "Explaining Multi-Model Queries"
---

The MM-cat framework uses MMQL (Multi-Model Query Language) to write queries over system-intpendent schema. The query is then translated into system-specific queries for each database system (PostgreSQL, MongoDB, Neo4j), which are individually executed and the results are combined into a single result.

The goal of the project is to extend the MM-cat tool with the funkcionality of retrieving and displaying detailed information about the MMQL queries. The main focus is on displaying individual query plans, i.e.:
- The price of the plan and of its individual operations.
- Which entities are used in the query and how they are joined to get the final results.
- Other query operations (filtering, sorting, etc.).
- How is translated into queries for specific database systems (PostgreSQL, MongoDB, Neo4j), the plans of these queries.

The module will consist both of a backend service and a user-friendly frontend GUI.

**This project is currently available.**