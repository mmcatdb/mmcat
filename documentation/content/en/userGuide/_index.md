---
title: "Overview"
description: "TODO"
---

This is a user documentation for the frontend application of the [MM-cat framework](https://gitlab.mff.cuni.cz/contosp/evolution-management). Please be aware that the application is still in the development so it may not be as polished as you might be used to from other applications. Especially the UX was given less attention so please read this documentation carefully.

## Functions

There are several buttons in the navigation menu on the left side.

### Jobs

A job is some data transformation process that is executed on the server. On [this page](jobs.md) you can see all existing jobs. You can check their state, start them or [create a new ones]().

### Mappings

A mapping defines both some logical model and how it is mapped to the conceptual schema. On [this page](mappings.md) are displayed all available mappings. There is also a link to the page where you can [create new mappings]().

### Schema Category

[There](schemaCategoryEditor.md) is a tool for creating and editing the conceptual schema.

### Instance Category

[There]() you can browse the instance category, which is created automatically when you run any job that imports data from database.

### Models

A model is a result of the opposite process, ie. of exporting data from the instance category to a specific database. It is basically a script that, when executed by the target database application, creates database with the exported data. On [this page](models.md) are displayed all generated models.

### Databases

A database in the sense of this application is a representation of some existing database to which the application can connect. This means it has to specify all the necessary connection information. On [this page](databases.md) you can see all the available databases, edit them or create a new ones.