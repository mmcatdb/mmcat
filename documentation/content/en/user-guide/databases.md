---
title: "Databases"
weight: 40
---

Here you can find all databases that you can import data from or export data to.

## Create

Click on the `Create new` button and specify all the necessary information. Then click on the `Add` button.

### Type

The framework currently supports two database engines - MongoDB and PostgreSQL. They need slightly different connection credentials (MongoDB requires an authentication database). All other information is common to both types.

### Label

A user-readable identifier of the database. It will appear in the system under this name.

![Edit database](/img/database.png)

## Edit

Editing a database is very similar to creating it. However, you can not change its type because there might already be mappings that depend on this specific database.

### Password

The second difference is that for security reasons the password can not leave the server. If you leave the field empty, the password will stay the same. On the other hand, if you input anything, even whitespace, the password will be changed.

## Delete

If the database is not involved in any mapping, it can be deleted by clicking on the `Delete` button on the edit page.