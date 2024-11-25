---
title: "SWAPI Dataset"
weight: 0
---

SWAPI (Star Wars API) is a web-based API providing structured and detailed information about the Star Wars universe, covering a variety of entities and their interrelations. The SWAPI dataset covers all the available data from the API.

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





