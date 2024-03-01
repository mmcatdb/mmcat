---
title: "Motivation"
---

There are a lot of different database systems and each has different advantages and disadvantages. Although they might appear to be similar, there are no standards between them that would allow us to use them interchangeably. There also is not any universal way how to transform data from one system to another or how to query among data from more than one database model. Moreover, this lack of standardization makes any effort for a general evolution tool almost impossible.

The multi-model databases partially solve this issue by allowing multiple different models (e.g. relational, document, key-value, graph, ...) to coexist within one database engine. There are multiple ways how to combine these models, for example:
- one model can be embedded in another,
- the entities from one model can reference the entities from another,
- the same data can be stored in multiple models, i.e. redundant.

But there is still no universally applicable way how to model the multi-model data or how to transform data from one model to another. The mmcat framework attempts to create a platform-independent conceptual model as an abstract layer above all the platform-dependent logical models. This solution, which is based on the [category theory](theoretical-background/category-theory.md), was theoretically developed by the article[^article].

This approach allows us to:
- model the data from both multiple models and multiple database systems at once,
- choose how we want to combine them (the framework supports all the possibilities mentioned above),
- query over all the data,
- migrate the data between models,
- evolve the model and, through it, the data itself.
And that all should be possible without the need to know all the specifics of the underlying database systems.

[^article]: Koupil, P., Holubová, I. A unified representation and transformation of multi-model data using category theory. *J Big Data* **9**, 61 (2022). https://doi.org/10.1186/s40537-022-00613-3