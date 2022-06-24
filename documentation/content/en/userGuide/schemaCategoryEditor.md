---
title: "Schema category editor"
description: "TODO"
---

TODO image

The schema category is displayed as a graph in the center of the page. You can add new objects an morphisms or the current ones. Be aware there are some limitations due to the nature of the conceptual schema (basically you can't make changes that could break existing mappings). However, we plann to implement procedures which would allow even these breaking changes.

Also mind that all changes must be explicitly saved by the `Save` button.

## Display

Each object is represented by node with it's label. The nodes can be moved by dragging them with the cursor, however the new positions must be explicitly saved by the `Save positions` button.

Each base morphism is an arow labeled by its signature. For each of those morphism there is a dual which isn't displayed for better clarity. Identity morphisms aren't displayed unless they are specifically created (again, for the clarity reasons). Composite morphisms can't be created by the tool directly and therefore aren't displayed in any case.

The colored dashed lines around the nodes shows in which databases the corresponding objects are represented. If an object isn't in any, there is a red circle around its node.

## Object

### Add

To create an object, click on the `Add object` button and specify its label and key (an internal identifier, must be unique among all objects). After the object is created its label is red which means the object is invalid because it doesn't have any id.

### Edit

When you click on any object you can delete it (the `Delete` button) or edit its label (which must be confirmed by the `Confirm` button). You can also add an id by clicking on the plus sign.

# Morphism

### Add

### Edit

## Other functions

The other buttons currently don't do anything. Their functionalities will be implemented in the evolution management update.