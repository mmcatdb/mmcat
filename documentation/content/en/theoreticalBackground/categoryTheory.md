---
title: "Category Theory"
math: true
weight: 10
---

A category consists of a set of objects, a set of morphisms and a composition operation over the morphisms. We can imagine it as an oriented multigraph - the objects are the nodes and the morphisms are the oriented edges.

The operation allows us to combine morphisms. If we have morphism $f$, ie. an edge from object $A$ to object $B$ (which can be written as $f: A \rightarrow B$), and another morphism $g: B \rightarrow C$, we can combine them to the morphism $g \circ f: A \rightarrow C$. The whole category is closed under this operation. This implies there is an idendity morphism $i_A: A \rightarrow A$ for each object $A$. The direct morphisms (i.e. from one object to a different one) are called base morphisms. Those created by composition are called composite.

The current version of the MM-cat framework requires that for each morphism which isn't an identity morphism there is an opposite morphism (called dual). For example, if there is a morphism $f: A \rightarrow B$, there must exist a morphism $f': B \rightarrow A$.