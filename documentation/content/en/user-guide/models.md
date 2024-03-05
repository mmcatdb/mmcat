---
title: "Models"
weight: 70
---

Any Category-to-Instance job produces a model which contains platform specific commands to create the exported data structure in given database system. It generally consists of multiple DDL and DML statements. Click on the model name to see the statements.

## Statements

Schema-less databases (e.g. MongoDB) usually have trivial DDL statements while all the important information is in the DML ones. Other databases define the schema first by the DDL statements and then fill it by the data from the DML ones.

This behavior has a few implications. If you run the export jobs while the instance database is empty, the DDL statements will be created as usual because they do not (in most cases) do not depend on the actual data. The exception is properties with dynamic names. However, the DML statements will be missing.

If you run a job multiple times, each iteration will delete the previous model and create a new one.

## Integrity constrains

The application currently does not support integrity constrains. However, the algorithms that would allow this functionality are already derived, so it is only a matter of time before they will be implemented.
