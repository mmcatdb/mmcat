---
title: "Schema Category"
weight: 10
---

The schema category is displayed as a graph in the center of the page. You can add new objects and morphisms or the current ones. Be aware there are some limitations due to the nature of the conceptual schema (basically you can not make changes that could break existing mappings). However, we plan to implement procedures that would allow even these breaking changes.

Also, mind that all changes must be explicitly saved by the `Save` button.

![Schema Category editor](/schema-category.png)
<!-- ![Schema Category editor](/static/img/schema-category.png) -->

## Display

Each object is represented by a node with its label. The nodes can be moved by dragging them with the cursor, however the new positions must be explicitly saved by the `Save positions` button.

Each base morphism is an arrow labeled by its signature. For each of those morphisms there is a dual which is not displayed for better clarity. Identity morphisms are not displayed as well (again, for the clarity reasons). Composite morphisms can not be created by the tool directly and therefore are not displayed in any case.

The colored dashed lines around the nodes shows in which databases the corresponding objects are represented. If an object is not in any, there is a red circle around its node.

The `Center graph` button move the objects to the center of the view. However, it does not change their positions, it just moves the whole graph.

## Objects

### Add

To create an object, click on the `Add object` button and specify its label. After the object is created its label is red which means the object is invalid because it does not have any id.

### Edit

When you click on any new object you can delete it (the `Delete` button) or edit its label (which must be confirmed by the `Confirm` button). You can also add an id by clicking on the plus sign.

## ID

Id is a set of properties (usually other objects) whose values unambiguously identify given object. These properties are represented as signatures of morphisms that connect the object with them. There are three types of ids.

An object can have multiple ids. The ids do not have to share types.

### Simple

A set with exactly one element. You can create it simply by [specifying a signature](createSignature.md). The corresponding morphism must have cardinalities:
- Object to Property - `1..1`, so there is exactly one identifier for any object.
- Property to Object - `0..1` or `1..1`, so there is at most one object for any identifier.

### Complex

A set with more than one element. The process of specifying signatures is the same as for the simple id, you just have to do it multiple times. There is also difference in cardinality - each morphism must fulfill these conditions:
- Object to Property - `1..1`, so there is exactly one identifier for any object.
- Property to Object - `0..*` or `1..*`, because the property would be a unique identifier otherwise. There would be no need for the complex id since a simple one would be sufficient.

### Empty

An empty set. This means that the object is identified by itself. In order to do so it has to be a pure value object.

## Morphisms

### Add

First click the `Add morphism` button. Then you can specify the morphism domain and codomain objects by clicking on the nodes in the graph. A click on a selected node will unselect it.

The `Switch` button allows you to switch both nodes while everything else will stay the same.

Finally, select the cardinality of the morphism and its dual.

### Edit

New morphism can be edited in a similar way how they are created. You can delete them too. It is important to note that any change that could break newly created ids will result in deleting those ids.

## Other features

The other buttons currently do not do anything. Their functionalities will be implemented in the evolution management update.