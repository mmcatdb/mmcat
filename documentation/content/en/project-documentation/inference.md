---
title: "Inference"
weight: 30
---

The Inference functionality empowers users to effortlessly generate multi-model schemas from existing single-model datasets. By utilizing compose schema inference techniques and schema editing operations, complex datasets can be transformed into well-structured multi-model representations. Enabling the generation of generally usable multi-model data.

## System Integration

The integration of the inference functionality was achieved by incorporating **MM-infer**[^article]—a tool previously developed by our research team members—as a dedicated module within the system. This module leverages MM-infer’s schema inference capabilities, enabling the automatic generation of schema representations from single-model datasets.

A key component of this integration is the creation of a new **Schema Conversion Component**. This component is responsible for converting the output of MM-infer, the Record Schema Description (RSD), into the Schema Category format, while also managing the creation of Mapping.

Furthermore, the integration was enhanced by **Inference Editor** which enables real live editing of the inferred Schema Category.

And finally, the addition of the inference functionality showed the need for an improved version of the Mapping Editor.

Through this integration, users are now able to infer and adjust a Schema Category from any input data stored in a single-model format. 

## Class Design

Detailed class design and structure of MM-infer is documented separately in the tool's documentation, here we present the underlying classes that make up the Schema Conversion Component and the Inference Editor. 

### Schema Conversion Component

### `SchemaConverter`

Acts as a wrapper for handling the conversion of the Record Schema Description (RSD) into the Schema Category and Mapping. It serves as an intermediary class whose main responsibility is to encapsulate the RSD as its input and apply the conversion logic via the `convertToSchemaCategoryAndMapping()`. Upon receiving the RSD, this method applies a series of transformations secured by the following classes.

### `RSDToAccessTreeConverter`

Takes the input RSD structure, traverses it and creates an instance of a tree object called `AccessTreeNode`, whose nodes encompass all the necessary information for further steps.

### `AccessTreeToSchemaCategoryConverter`

Traverses the tree structure and converts it into a Schema Category.

### `MappingCreator`

Last, but not least, the information collected in the tree is also used for the creation of Mapping.

![Inference Sequence Diagram](/img/inference-seq-diagram.png)

### Inference Editor

If the inferred Schema Category is too complex and unreadable, the Inference Editor comes into play. The available edit operations are encompassed by these classes.

### `InferenceEditor`

The wrapper class for inferred Schema Category editing. Created with a list of instances of the `InferenceEdit` from which it creates the `InferenceEditAlgorithm` instances and applies the editing operations. 

![Inference Editor Sequence Diagram](/img/inference-editor-seq-diagram.png)

## Mapping Editor

The current version enables the user to work with multiple nodes at once. Furthermore, it offers context menu as well as keyboard shortcuts for smooth and quick work flow.


Notes:
- maybe also describe the AccessTreeNode class
- this can happen as many times as user requests (the inference editing, also we have undo/redo)
- seq diagram could be more complex (for example if I were to add the layout logic as well)
- in seq diagrams I dont show mapping (mapping creation, mapping editing in inference editor, mapping editor)
- add some pics of the mapping editor

[^article]: Koupil, P., Hricko, S., & Holubová, I. Mm-infer: A tool for inference of multi-model
schemas. (2022). https://doi.org/10.48786/EDBT.2022.52