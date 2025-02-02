---
title: "Selection"
math: true
weight: 10
---

*How to fill variables with values from the databases?*

## Variable Tree

The pattern in the `WHERE` clause is converted into a tree of variables. Each variable is mapped to a specific [schema object](schema-category.md#objects) (although multiple variables can be mapped to the same object). Edges of the tree represent the [morphisms](schema-category.md#morphisms) between the objects. Their direction is important - if we go along a morphism, it represents a property (with cardinality 1:1). If we go againts it, it represents an array (1:n).

{{< image src="/img/variable-tree.svg" alt="An example of variable tree." caption="An example of variable tree." >}}

### Expression

An expression is something that can be assigned a value. We use them to enable filters, and computed properties (with or without aggregation). There are three types of expressions:
- *Constant* - a constant value.
- *Variable* - a variable from the tree.
- *Function* - a function with an arbitrary number of *expression* arguments.

During the query resolution, we first fill the variables with values from the database. This structure, *query result*, is also a tree. Then we evaluate the functions and add them to the tree (as if they were variables). The reason is that we will need them for filters, and we might need them for projection. Constants are added during projection (because they are not needed before that).

### Evaluation

Let's start with the following example:

```sparql
FILTER(?limit > 100)
```

The real-life use case behind this filter might be that each customer has some kind of *limit* and we want to select only those customers whose limit is above 100. The expression is evaluated for each `?customer` and added as another leaf of the `?customer` node. We say that `?limit` is the *reference node* of this expression.

Now consider this example:

```sparql
FILTER(?price > 100)
```

It might look like that we are interested only customers who purchased high-value products. However, if we look at the whole query, the exact meaning of this filter is: *Find all orders for each customer. In these orders, include only those order items whose product costs more than "100".* So, in this case, the expression is evaluated for each `?item` and added as another leaf of the `?item` node. **In general, there must be a 1:1 path from a reference node to each argument of its expression.**

Ok, but how can we select only those customers who purchased high-value products? We can use *aggregations*. **A reference node of an aggregation function has to have a 1:n path to its argument.** So, let's try the following query:

```sparql
FILTER(max(?price) > 100)
```

Will it work as intended? Well, no. The reference node of this expression is `?order`, not `?customer`. Let's break it down. First, we need to evaluate The `max(?price)` expression. **The reference node of an expression is as low in the tree as possible.** If we choose `?customer` as a reference node, the path would be `-3.-5.6`. That is clearly a `1:n` path, but it isn't the shortest one. So the reference node must be `?order` with a path `-5.6` to `?price`. So, we add the result of the `max(?price)` expression as another leaf of the `?order` node. Then we evaluate the `max(?price) > 100` expression. The reference node is, again, `?order`, because it is just above the new `max(?price)` node. We can achieve the desired result by using double aggregation:

```sparql
FILTER(max(max(?price)) > 100)
```

{{< image src="/img/variable-tree-expressions.svg" alt="Variable tree with evaluated expressions." caption="Variable tree with evaluated expressions." >}}

However, this solution isn't ideal for several reasons:
- It's not very readable (especially with multiple nested aggregations).
- It's not very efficient and hard to optimize.
- If we change a path in the query, we have to change all the aggregations.
- Some times it's not even possible to use this approach, because we use recursive paths (so the path might be of an arbitrary length).

We can fix this by adding a second argument to the aggregation function, which will tell us which node is the reference node. The query would look like this:

```sparql
FILTER(max(?price, ?customer) > 100)
```

Lastly, let's consider this example:

```sparql
FILTER(?price > ?limit)
```

If we use all the rules we have defined so far, it is clear that the reference node of this expression is `?item`. This might seem counterintuitive at first, because the reference node is not a common root for all variables in the expression. However, there is a 1:1 path from `?item` to `?limit`, so it is a valid reference node.
