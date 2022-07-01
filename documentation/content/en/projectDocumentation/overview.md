---
title: "Overview"
weight: 0
---

This documentation describes only the high level concepts of the framework. It doesn't try to analyze technical details of the code or to define coding conventions.

## Disclaimer

Be aware that the application is meant to be purely a proof of concept. Although the computational requirements were taken into account when deciding what algorithms use and how to implement them, they were never consider to be a priority. The main purpose of the application is to show that the chosen approach for modelling multi-model databases is viable. The optimizations can be done later.

### Security

From the reasons mentioned above, the application doesn't use any security policy, meaning that it can be used by anybody without authorization. The only security mechanism is that the backend application doesn't expose passwords of the database accounts it uses to import data from.
