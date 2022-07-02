---
title: "Conceptual Schema"
math: true
weight: 20
---

Any application needs a way to represent some sort of data. A conceptual schema is a high-level model of the data from the application's point of view. It hides the technical details of physically storing the data from the application and allows us to concentrate on the concepts that are important for the domain we are working on. In the MM-cat framework this model is represented by a *schema category*. A similar structure capable of holding concrete data is called *instance category*.

On the other hand, a logical model describes how the data should be organized to tables (for a relational model), documents (for a document model) and so on. It generally does not depend on any specific database engine. However, it might be affected by the possibilities of the target type of database systems.

The framework is based on the condition that an application should have one conceptual schema which can be [mapped](mapping.md) to multiple logical models. The models can partially or completely overlap and each of them can be suited for a different database system.

## Schema category

A schema category is a category, which means it is a tuple of objects, morphisms and an composition operation over the morphisms.

### Objects

Each schema object corresponds to a kind or a property from the conceptual schema. It is a tuple of:
- *key* - an internal identifier,
- label - a user readable identifier,
- *superid* - a set of data attributes the object is expected to have,
- *ids* - a set of all identifiers of the object.
Each attribute of the superid is represented by a signature of morphism that goes from the object with the superid to the object which correspond to the attribute. So the superid is just a set of signatures. The concept of signature will be explained later.

An identifier is also a set of signatures, this time their morphisms leads to the objects that unambiguously identify given object. Hence, the ids is a set of sets of signatures. It must hold that all the signatures from all the sets in the ids must be also contained in the superid.

The most simple schema object is an object with only one identifier (an empty signature) and a single element superid (also the empty signature). So its ids and superid are $\{\{ \epsilon \}\}$ and $\{ \epsilon \}$. This means the object represents a simple property which is identified by its value. More complex objects are identified by and contain values of other objects.

### Morphisms

A morphism represents an oriented edge from one object to another. It is modeled as a tuple of:
- *signature* - an internal identifier,
- *domain* object - an object in which the edge starts,
- *codomain* object - an object in which the edge ends,
- *cardinality* - one of the following: `0..1`, `1..1`, `0..*`, `1..*`.
In the application the signatures of base morphisms are represented by integers. The convention is that if a morphism has signature $"n"$, its dual has a signature $"-n"$. The identity morphisms are represented by the *empty signature* $\epsilon$.

The signatures are concatenated as follows. Consider objects $A$, $B$ and $C$ with morphisms $f: A \rightarrow B$, $g: B \rightarrow C$ and corresponding signatures $\sigma_f = \texttt{5}$, $\sigma_g = \texttt{-7}$. If we combine them to the morphism $g \circ f: A \rightarrow C$, its signature will be
$$
\sigma_{g \circ f} = \sigma_g \circ \sigma_f = \texttt{-7.5} \ .
$$
The domain and codomain objects are represented by their identifiers, i.e. keys.

## Instance category

An instance category is a structure capable of holding actual data. It is organized in a same way as the schema category, however its entities have different contents.

### Objects

Consider a schema object $A$ with superid $\{ \sigma_f = \texttt{3}, \sigma_g = \texttt{6} \}$ where $f: A \rightarrow B$ and $g: A \rightarrow C$ reference the objects $B$ and $C$. Let is denote $t_i$ as one specific instance of values of these objects. Then the corresponding instance object can be considered as a set of all $t_i$ in our domain. One $t_i$ can be represented as a set of tuples $(\sigma, v)$, where $\sigma$ is one of the signatures and $v$ is a value from the domain of the object linked to $A$ with morphism with signature $\sigma$. So in this case a $t_i$ can be written as
$$
    t_i = \{(\sigma_f, b_j), (\sigma_g, c_k)\} \ .
$$
For example, $B$ can represent a first name with possible values `Frodo`, `Bilbo` and `Samwise` while $C$ represents a surname with possible values `Baggins`, `Gamgee` etc. All the possible $t_i$ in the set would be:

```js
{ 3: "Frodo", 6: "Baggins" },
{ 3: "Bilbo", 6: "Baggins" },
{ 3: "Samwise", 6: "Gamgee" }
```

One $t_i$ is called an *active domain row*. A set of all $t_i$ together creates an *active domain* (or *instance object*) of the schema object $A$, which can be denoted as $I(A)$.

It is important to note that the objects $B$ and $C$ must be the simple schema objects with the empty signature as an identifier, so we can reference them by their values. A complex object (e.g. $A$) can be also referenced, but we have to use one of its identifiers to do so.

### Morphisms

An instance morphism is a set of all relations between the rows from two instance objects. If $f: A \rightarrow B$ is a schema morphism with the domain object $A$ and the codomain object $B$, we can denote $m_i$ to be a specific relation between selected two rows $a_j \in I(A)$ and $b_k \in I(B)$. In other words, $m_i$ is an ordered pair $m_i = (a_j, b_k)$. The instance morphism of $f$, denoted as $I(f)$, is then the set of all possible $m_i$.

If we go back to the previous example, all the $m_i \in I(f)$ would look like:

```js
( { 3: "Frodo", 6: "Baggins" }, { ε: "Frodo" } ),
( { 3: "Bilbo", 6: "Baggins" }, { ε: "Bilbo" } ),
( { 3: "Samwise", 6: "Gamgee" }, { ε: "Samwise" } )
```
