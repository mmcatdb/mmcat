---
title: "Unified Interface for Accessing Multi-Model Data"
---

There are many different ways how to access data:
- A database system (PostgreSQL, MongoDB, Neo4j, ...).
- A file (CSV, JSON, ...). The file itself can be accessed in multiple ways (local file, remote file, api endpoint, ...).
- An API endpoint (REST, GraphQL, ...).
Some of these ways are naturally more suitable for specific applications. E.g., if we want to infer a schema of the data, it doesn't matter if it's a document in MongoDB or a JSON file. However, for querying the data, the database is much better. The MM-cat framework deals with this complexity by using abstract wrappers for the required features (reading, writing, querying, ...). However, they currently cover only the database systems.

The goal of this project is to design a unified interface for accessing data and then implement it for various datasources. An important part of the project is a frontend GUI that allows the user to easily setup the sources by providing their connection details. Lastly, an ability to test the connection and retrieve metadata (e.g., file size, number of entities, ...) is expected.

**This project is currently available.**