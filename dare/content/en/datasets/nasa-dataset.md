---
title: "NASA Dataset"
weight: 0
---

The [NASA dataset](https://data.nasa.gov/Software/NASA-open-source-code-projects-with-A-I-generated-/3efg-u4v8/about_data) consists of a single large JSON file called `code_projects.json` detailing NASA's various code projects. The inferred schema has a unique structure because of the nature of the file - it includes a single document with various attributes one of which is an array of subdocuments each representing a NASA's code project.

![NASA dataset](/img/nasa-dataset-sk.png)

## Initial Dataset Specifications

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Data Link</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Code_projects</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/nasa/input/code_projects.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="1" file="nasa/nasa-in" label="Mapping" >}}</td>
    </tr>
  </tbody>
</table>

## Generated Dataset Specifications

### Case A: Transforming NASA Dataset into Relational Tables in PostgreSQL

Extract relevant fields from the JSON and convert them into a relational schema, for example a project and tags schemas. The relational structure reduces data redundancy by storing tags separately from project details, ensuring a more compact and maintainable database. PostgreSQL's indexing and query optimization allow efficient retrieval of project details based on tags or vice versa, even as the dataset grows. Updates to project information or tags are straightforward and won't impact unrelated data. 
This approach makes the NASA dataset structured, easier to query, and better suited for integration with other relational data sources.

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Output Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Project</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="2" file="nasa/nasa-A-project" label="Output Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Tags</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="3" file="nasa/nasa-A-tags" label="Output Mapping" >}}</td>
    </tr>
  </tbody>
</table>

*Note*: While it is technically possible to define a mapping that creates realtion tables from an array within a JSON file, it is currently not possible to generate the transformed data. This limitation arises because the Transformation modules in MM-cat are not yet equipped to handle array objects effectively. Therefore, we do not provide the transformed dataset at this stage. Enhancements to the Transformation modules to address this limitation are planned for **future development**.