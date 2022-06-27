# Overview

MM-cat is a framework for modelling multi-model data. It is based on the category theory which is abstract enough to contain all currently used data models. The necessary theoretical background is described in the article [1]. The brief summary of the most important concepts is [here](overview.md#category-theory).

The generality of the theory allows us to create a category which describe multiple logical models from different databases at once. Thus we are able to model any combination of different logical schemas with one category. This object is called [schema category](overview.md#schema-category).

A next step is to use the algorithms the framework provides to import data from all the databases to one place called [instance category](overview.md#instance-category). There are also algorithms which can export the data from the *instace category* to specific databases.

The advantage of this approach is this - we can convert data from any database **A** to any other database **B** simply by transforming them from **A** to the *instance category* and from ther to **B**. The only thing we need is to create a wrapper for each involved database system that would handle the platform specific requirements of given database.

Each logical model is mapped to the schema category via its own [mapping](overview.md#mapping). This structure defines both the mapping and the logical model itself.

## Category theory

A category consists of a set of objects, a set of morphisms and a composition operation over morphisms. We can imagine it as an oriented multigraph - the objects are the nodes and the morphisms are the oriented edges.

The operation allows us to combine morphisms. If we have morphism $f$, ie. an edge from object $A$ to object $B$ (which can be written as $f: A \rightarrow B$), and another morphism $g: B \rightarrow C$, we can combine them to the morphism $g \circ f: A \rightarrow B$. The whole category is closed under this operation. This implies there is an idendity morphism $i_A: A \rightarrow A$ for each object $A$. The direct morphisms (i.e. from one object to a different one) are called base morphisms. Those created by composition are called composite.

The current version of the MM-cat framework requires that for each morphism which isn't an identity morphism there is an opposite morphism (called dual). For example, if there is a morphism $f: A \rightarrow B$, there must exist a morphism $f': B \rightarrow A$.

## Schema category

TODO

## Instance category

TODO

## Mapping

TODO

## References

[1] Koupil, P., Holubová, I. A unified representation and transformation of multi-model data using category theory. *J Big Data* **9**, 61 (2022). https://doi.org/10.1186/s40537-022-00613-3

