---
title: "Schema Category"
description: "TODO"
weight: 30
---

TODO image

The schema category is displayed as a graph in the center of the page. You can add new objects an morphisms or the current ones. Be aware there are some limitations due to the nature of the conceptual schema (basically you can't make changes that could break existing mappings). However, we plan to implement procedures which would allow even these breaking changes.

Also mind that all changes must be explicitly saved by the `Save` button.

## Display

Each object is represented by node with it's label. The nodes can be moved by dragging them with the cursor, however the new positions must be explicitly saved by the `Save positions` button.

Each base morphism is an arow labeled by its signature. For each of those morphism there is a dual which isn't displayed for better clarity. Identity morphisms aren't displayed unless they are specifically created (again, for the clarity reasons). Composite morphisms can't be created by the tool directly and therefore aren't displayed in any case.

The colored dashed lines around the nodes shows in which databases the corresponding objects are represented. If an object isn't in any, there is a red circle around its node.

The `Center graph` button move the objects to the center of the view. However, it doesn't change their positions, it just moves the whole graph.

## Objects

### Add

To create an object, click on the `Add object` button and specify its label. After the object is created its label is red which means the object is invalid because it doesn't have any id.

### Edit

When you click on any new object you can delete it (the `Delete` button) or edit its label (which must be confirmed by the `Confirm` button). You can also add an id by clicking on the plus sign.

## ID

Id is a set of properties (usually other objects) whose values unambiguously identify given object. These properties are represented as a signatures of morhpisms that connect the object with them. There are three types of ids.

An object can have multiple ids. The ids don't have to share types.

### Simple

A set with exactly one element. You can create it simply by [specifying a signature](createSignature.md). The corresponding morphism must have cardinalities:
- Object to Property - 1..1, so there is exactly one identifier for any object.
- Property to Object - 0..1 or 1..1, so there is at most one object for any identifier.

### Complex

A set with more than one elements. The process of specifying signatures is the same as for the simple id, you just have to do it multiple times. There is also difference in cardinality - each morphism must fulfill these conditions:
- Object to Property - 1..1, so there is exactly one identifier for any object.
- Property to Object - 0..* or 1..*, because the property would be a unique identifier otherwise. There would be no need for the complex id since a simple one would be sufficient.

### Empty

An empty set. This means that the object is identified by itself. In order to do so it has to be a pure value object.

## Morphisms

### Add

First click the `Add morphism` button. Then you can specify the morphism domain and codomain objects by clicking on the nodes in the graph. A click on a selected node will unselect it.

The `Switch` button allows you to switch 

To create an identity morphism, select the domain object and then click on the `Select same object` button. Although there can be multiple morphisms between any any two objects, multiple identity morphisms on one object aren't supported due to a bug in the graphic library.

Finally select the cardinality of the morphism and its dual.

### Edit

New morphism can be edited in a similar way how they are created. You can delete them too. It's imortant to note that any change that could break newly created ids will result in deleting those ids.

## Other functions

The other buttons currently don't do anything. Their functionalities will be implemented in the evolution management update.