# transformations

This package contains the algorithms for importing and exporting data between the instance category and an arbitrary database system. The category is defined by the [core](../core/README.md) package while the database systems are represented by the [abstractwrappers](../abstractwrappers/README.md) package and implemented by the [wrappers](../wrappers/README.md) package.

The currently implemented algorithms are:
- MTC (Model to Category) - imports the data to the instance category from a database system,
- DDL (Data Definition Language) - exports the schema of the data from the instance category in the form of DDL statements,
- DML (Data Manipulation Language) - exports the data from the instance category in the form of DML statements.

The following algorithm is yet to be implemented:
- IC (Integrity Constrains) - exports the identity constraints from the instance category in the form of IC statements.
