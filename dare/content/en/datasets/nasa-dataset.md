---
title: "NASA Dataset"
weight: 0
---

The NASA dataset consists of a single large JSON file called `code_projects.json` detailing NASA's various code projects. The inferred schema has a unique structure because of the nature of the file - it includes a single document with various attributes one of which is an array of subdocuments each representing a NASA's code project.

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
