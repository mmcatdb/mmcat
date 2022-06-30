---
title: "Mappings"
weight: 30
---

Here you can find all mappings that can be used in import or export jobs. They are displayed with their access path in the JSON-like structure described [here](../theoreticalBackground/mapping.md#representation).

## Create

First click on the `Create new` button. Then select the database from the select box and the root object from the nodes in the graph. Both values can't be changed after you click on the `Confirm` button.

The displayed graph is described [here](schemaCategoryEditor.md#display).

Now you can change the name of the mapping or the name of the kind (i.e. document, table, ...). You can also add properties to the path.

### Access path

The access path of the mapping is displayed on the right. To add a subproperty to any existing property or to the root property, click on the plus button.

First you have to [select the signature](createSignature.md) of the property. The default value of the signature is the null signature which corresponds to the auxiliary property.

If the type of the property can't be determined from the current information, you will be asked to specify it. There are multiple rules:
- Some databases (eg. PostgreSQL) don't allow to create complex properties.
- Auxiliary property has to be complex.
- If the corresponding category object doesn't have any simple identifier (all objects have to have at least one identifier) it has to be complex.

Then you have to choose a name of the property (or name of the column etc.) in the chosen database system. It can be static, dynamic or anonymous. If the dynamic name is chosen, you have to specify its signature.

Lastly, click on the `Finish` button to add the property.

### Edit property

The already existing properties can be edited by clicking on the property name in the JSON-like structure on the right. The process is almost the same as to create a property, however at every step you can choose to keep current values. **Be aware that any change in property signature inevitably leads to deleting all of its subproperties!**

You can also delete a property manually by clicking on the `Delete` button. In this case its subproperties will be deleted as well.
