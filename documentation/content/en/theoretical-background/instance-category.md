---
title: "Instance Category"
math: true
weight: 25
---

An instance category is a structure capable of holding actual data. It is organized in a same way as the schema category, however its entities have different contents.

### Objects

Consider a schema object $A$ with superid $\{ \sigma_f = \texttt{3}, \sigma_g = \texttt{6} \}$ where $f: A \rightarrow B$ and $g: A \rightarrow C$ reference the objects $B$ and $C$. Let us denote $t_i$ as one specific instance of values of these objects. Then the corresponding instance object can be considered as a set of all $t_i$ in our domain. One $t_i$ can be represented as a set of tuples $(\sigma, v)$, where $\sigma$ is one of the signatures and $v$ is a value from the domain of the object linked to $A$ with morphism with signature $\sigma$. So in this case a $t_i$ can be written as
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
