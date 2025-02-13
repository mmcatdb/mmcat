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

An expression is something that can be assigned a value. We use them to enable filters and computed properties (with or without aggregation). There are three types of expressions:
- *Constant* - a constant value.
- *Variable* - a variable from the tree.
- *Computation* - a function with an arbitrary number of *expression* arguments.

During the query resolution, we first fill the variables with values from the database. This structure, *query result*, is also a tree. Then we evaluate the computations and add them to the tree (as if they were variables). The reason is that we will need them for filters, and we might need them for projection. Constants are added during projection (because they are not needed before that).

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

It might look like that we are interested only customers who purchased high-value products. However, if we look at the whole query, the exact meaning of this filter is: *Find all orders for each customer. In these orders, include only those order items whose product costs more than "100".* So, in this case, the expression is evaluated for each `?item` and added as another leaf of the `?item` node. **In general, there must be a 1:1 (or n:1) path from a reference node to each argument of its expression.**

Notice that it doesn't really matter which which node *exactly* is the reference node, as long as there is the *:1 path. However, for implementation purposes, we want to choose one. If the expression uses only one variable, it is the reference node. If it uses multiple variables, it is the one closest to them (it should be easy to see there is always only one such node). If none variables are used (e.g., `FILTER(1 > 0)`, for whatever reason), the reference node is undefined (meaning that the result is accessible in each node of the tree).

Let's consider this example:

```sparql
FILTER(?price > ?limit)
```

If we use all the rules we have defined so far, it is clear that the reference node of this expression is `?item`. This might seem counterintuitive at first, because the reference node is not a common root for all variables in the expression. However, there is a 1:1 path from `?item` to `?limit`, so it is a valid reference node.

### Aggregation

Ok, but how can we select only those customers who purchased high-value products? We can use *aggregations*. **The reference node of an aggregation function has to have a 1:n path to its argument.** So, it might be tempting to try the following query:

```sparql
FILTER(max(?price) > 100) # Don't even try this!
```

Will it work as intended? Well, no, for two reasons:
1. **By default, the reference node for an aggreagation is the whole query.** We can imagine a virtual `?query` node above the root. It has by definition a 1:n path to all instances of the root variable. So, this filter would either accept or reject all customers at once. We can use `GROUP BY ?customer` to change the reference node to `?customer`.
2. **The `FILTER` expressions can't use aggregation functions.** If it was possible, the result would depend on the order of the filter operations, which is not allowed.
    
    E.g., if we used these two filters
    ```sparql
    FILTER(?price > 100)
    FILTER(sum(?price) > 200) # This is also forbidden!
    ...
    GROUP BY ?customer
    ```
    on a customer with items for `[ 50, 100, 150 ]`, one of two situations would happen:
    1. The first filter is applied first and removes all items except `150`. The second filter then rejects the customer, because the sum is `150`.
    2. The second filter is applied first and accepts the customer, because the sum is `300`. The first filter then removes all items except the `150` one.
    
    In order to fix this issue, we have to move the second filter to the `HAVING` clause.

So, the final condition should look like this:

```sparql
GROUP BY ?customer
HAVING(max(?price) > 100)
```
{{< image src="/img/variable-tree-expressions.svg" alt="Variable tree with evaluated expressions." caption="Variable tree with evaluated expressions." >}}

What if we wanted to *first* filter out all orders with total price less than 100 and *then* select only those customers who have at least three such orders? We can't use simple `GROUP BY` for this, because the first condition requires `?order` to be the reference node, while the second condition requires `?customer`. So, we have to use a nested query, which allows us to separate the reference nodes for each condition.

Unfortunatelly, it is not possible to use a different reference node for each `HAVING` condition in a single query. The reason is that if it would be possible, the result would, again, depend on the order of the `HAVING` operations. We can still use multiple variables in one `GROUP BY` clause. Nevertheless, in that case, there must be an n:m path from each reference node to each other. Otherwise, the nodes with a *:1 path to others would not have any effect on the result - just imagine grouping by `?order` and then by `?customer`.
