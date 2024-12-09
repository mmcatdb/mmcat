---
title: "SWAPI Dataset"
weight: 0
---

[SWAPI (Star Wars API)](https://swapi.dev/) is a web-based API providing structured and detailed information about the Star Wars universe, covering a variety of entities and their interrelations. The SWAPI dataset covers all the available data from the API.

It is structured in JSON format. The files are the following:
- `people` with detailed information about characters
- `films` with information about the Star Wars movies
- `planets` with details about planets
- `species` with characteristics of species
- `vehicles` with specifications of vehicles
- `starhips` with similar information as `vehicles`, but some added details

![SWAPI dataset](/img/swapi-dataset-sk.png)

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
      <td style="border: 1px solid #ddd; padding: 8px;">People</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/swapi/input/people.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="1" file="swapi/swapi-in-people" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Films</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/swapi/input/films.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="2" file="swapi/swapi-in-films" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Planets</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/swapi/input/planets.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="3" file="swapi/swapi-in-planets" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Species</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/swapi/input/species.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="4" file="swapi/swapi-in-species" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Vehicles</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/swapi/input/vehicles.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="5" file="swapi/swapi-in-vehicles" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Starships</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/swapi/input/starships.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="6" file="swapi/swapi-in-starships" label="Mapping" >}}</td>
    </tr>
  </tbody>
</table>

## Generated Dataset Specifications

### Case A: Embedding Film Data with Related Details from Referenced Files in MongoDB

The film data is enriched by embedding detailed information from all referenced entities. Instead of storing only references (URLs) to related data, the selected details of these references are included as embedded objects within the film documents. The transformed dataset is then stored in MongoDB, with each film represented as a comprehensive, self-contained document.

By embedding all related details into each film document and storing it in MongoDB, the transformed SWAPI dataset becomes highly accessible, efficient, and scalable. It supports a wide range of applications, from backend APIs to analytical tools, and aligns well with MongoDB’s strengths in handling nested, hierarchical data.

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Output Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Films</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="7" file="swapi/swapi-A-films" label="Output Mapping" >}}</td>
    </tr>
  </tbody>
</table>

*Note*: While it is technically possible to define a mapping that embeds the film data with details from referenced entities, it is currently not possible to generate the transformed data. This limitation arises because the Transformation modules in MM-cat are not yet equipped to handle array references effectively. Therefore, we do not provide the transformed dataset at this stage. Enhancements to the Transformation modules to address this limitation are planned for **future development**.






