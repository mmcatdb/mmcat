# MM-evocat

A multi-model data modelling and evolution framework based on category theory. It consists of the following modules:
- [Core](./core/README.md) defines classes needed by other modules.
- [Transformations](./transformations/README.md) contains algorithms for transforming data from logical to conceptual model (and back).
- [Wrappers](./wrappers/README.md) provides funcionalities specific for different databases.
- [Evolution](./evolution/README.md) will be implemented later and it will include algorithms for the evolution of both the conceptual and the logical models.
- [Backend application](./server/README.md) provides an API which exposes the functionality of the other modules. It also contains a job scheduler for the transformation algorithms.
- [Frontend application](./example-ui//README.md) is used as a UI for the backend API. It is also a tool for modelling all the necessary data structures which are then used by the other modules.

## Installation

- Make sure you have all the required software for both [Backend](./server/README.md#requirements) and [Frontend](./example-ui/README.md#configuration) applications.
- To compile the java modules and install dependencies, run:
```sh
mvn install -Dmaven.test.skip
```
- Then follow the steps in the [Backend](./server/README.md) and [Frontend](./example-ui/README.md) guides.
- Lastly, you need a web server to make both applications available. See the [Apache configuration](./examples/apacheConfiguration.md) for detailed instructions.

# Design

## Data class hierarchy

### Info

- Contains basic information about the resource:
    - id
    - label
    - description (optional)
- Cannot contain information about other resources.
- Corresponds to an **Info** class on client, which is displayed by a **Preview** component.

### Full

- Extends the **Info** class with information about neighbour resources (in the form of their **Infos**).
- Cannot contain **Fulls** of other resources.

## Ids, Signatures ...

- **Signature** describes a path in a graph. There are three distinct types:
    - *Empty* - the object itself (i.e., the value is stored on the object),
    - *Base* - a direct neighbour to the object via a morphism with the signature (i.e., the value is stored on the neighbour),
    - *Composite* - a composition of base signatures.
- A **SchemaObject** has two properties:
    - *ids* - a description of how the object is identified,
    - *superId* - a set of all attributes whose values we want to capture on the object.
- The *ids* property is one of the following:
    - *Value* - the object is identified by its (string) value, i.e., by the data itself,
    - *Generated* - the object is, again, identified by its value, however it is automatically generated (if not present in the data)
        - TODO - maybe it should be non-accessible so it would be always generated
    - *Signature* - its a set of **SignatureId**s where each of them is a set of **Signature**s leading to the objects with the values.
- The *superId* is just a set of signatures, because we do not have to know what they represent - the only important thinkg is how to get to them.
