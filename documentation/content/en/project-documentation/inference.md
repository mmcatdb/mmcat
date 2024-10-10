---
title: "Inference"
weight: 30
---

The Inference functionality empowers users to effortlessly generate multi-model schemas from existing single-model datasets. By utilizing compose schema inference techniques, complex datasets can be transformed into well-structured multi-model representations. Enabling the generation of generally usable multi-model data.

## System Integration

The integration of the inference functionality was achieved by incorporating **MM-infer**—a tool previously developed by research team members (add a reference here??)—as a dedicated module within the system. This module leverages MM-infer’s schema inference capabilities, enabling the automatic generation of schema representations from single-model datasets.

A key component of this integration is the creation of a new **Schema Conversion Component**. This component is responsible for converting the output of MM-infer, the Record Schema Description (RSD), into the Schema Category format, while also managing the creation of the Mapping.

Through this integration, users are now able to infer a Schema Category from any input data stored in a single-model format. 


## Class Design

Detailed class design and structure for MM-infer are documented separately in the tool's documentation, here we focus on the underlying classes that make up the Schema Conversion Component. These classes are responsible for transforming a Record Schema Description (RSD), into a Schema Category and a Mapping.

### `SchemaConverter`

This class acts as a wrapper for handling the conversion of the Record Schema Description (RSD) into the Schema Category and Mapping. It serves as an intermediary class whose main responsibility is to encapsulate the RSD as its input and apply the conversion logic via the `convertToSchemaCategoryAndMapping()`. Upon receiving the RSD, this method applies a series of transformations secured by the following classes.

### `RSDToAccessTreeConverter`

Takes the input RSD structure, traverses it and while encompassing all the necessary information creates a tree.

### `AccessTreeToSchemaCategoryConverter`

Next, the tree is traversed and the corresponding Schema Category gets created.

### `MappingCreator`

(Now I see I might need a different name for this class??)
Last, but not least, the information collected in the tree is also used for the creation of Mapping.


Now ad inference Sequence Diagrams (and maybe some other: SK display, SK edits, mapping creation??)