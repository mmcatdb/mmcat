---
title: "Overview"
weight: 0
---

This documentation describes only the high-level concepts of the framework. It does not try to analyze technical details of the code or define coding conventions.

## Architecture

The framework is divided into several modules.

![Framework architecture](/overall-architecture.png)

### `core`

This module defines the basic objects and structures the rest of the framework relies on. It is represented by the yellow boxes, but it is used in all other modules.

### `transformations`

The two blue boxes include several algorithms, which form the main part of the `transformations` module. Their purpose is to transform data from the databases to the categorical representation and back.

### `abstractWrappers`

Depicted in gray, these classes hide the specific databases and provide a platform-independent interface that is used by the algorithms from the `transformations` module.

### `wrappers`

Each supported database system has its own wrappers which are a concrete implementation of the `abstractWrappers` module. These communicate directly with the drivers of the selected database. Currently, the PostgreSQL and MongoDB systems are supported.

### `server`, ` example-ui`

The red boxes represent all high-level operations the user can do with the framework:
- querying over the categorical representation,
- inference of categorical schema from the actual data,
- migration of data from one database system to another,
- evolution management of the categorical representation.

Currently, only the data migration operation is implemented fully, but the others are going to be as well. All the operations will be accessible via the [backend](backend.md) and [frontend](frontend.md) applications which are located in the `server` and `example-ui` modules.

## Disclaimer

Be aware that the application is meant to be purely a proof of concept. Although the computational requirements were taken into account when deciding what algorithms to use and how to implement them, they were never considered to be a priority. The main purpose of the application is to show that the chosen approach for modeling multi-model databases is viable. The optimizations can be done later.

### Security

For the reasons mentioned above, the application does not use any security policy, meaning that it can be used by anybody without authorization. The only security mechanism is that the backend application does not expose passwords of the database accounts it uses to import data from.
