---
title: "General Concepts"
math: true
weight: 0
---

MM-evocat is a framework for modelling multi-model data. It is based on category theory which is abstract enough to contain all currently used data models. The necessary theoretical background is described in the article[^article]. The brief summary of the most important concepts is [here](categoryTheory.md).

The generality of the theory allows us to create a category which describe multiple logical models from different databases at once. Thus, we are able to model any combination of different logical schemas with one category. This object is called [schema category](schemaCategory.md).

The next step is to use the algorithms the framework provides to import data from all the databases to a unified representation called [instance category](./instanceCategory.md). There are also algorithms which can export the data from the *instance category* to specific databases.

The advantage of this approach is this - we can convert data from any database **A** to any other database **B** simply by transforming them from **A** to the *instance category* and from there to **B**. The only thing we need is to create a wrapper for each involved database system that would handle the platform specific requirements of given database.

Each logical model is mapped to the schema category via its own [mapping](mapping.md). This structure defines both the mapping and the logical model itself.



[^article]: Koupil, P., Holubová, I. A unified representation and transformation of multi-model data using category theory. *J Big Data* **9**, 61 (2022). https://doi.org/10.1186/s40537-022-00613-3
