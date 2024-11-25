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

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">People: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/swapi/input/people.json" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-1" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;birth_year: 0,<br>&nbsp;&nbsp;created: 1,<br>&nbsp;&nbsp;edited: 2,<br>&nbsp;&nbsp;eye_color: 3,<br>&nbsp;&nbsp;gender: 7,<br>&nbsp;&nbsp;hair_color: 8,<br>&nbsp;&nbsp;height: 9,<br>&nbsp;&nbsp;homeworld: 10,<br>&nbsp;&nbsp;mass: 11,<br>&nbsp;&nbsp;name: 12,<br>&nbsp;&nbsp;skin_color: 13,<br>&nbsp;&nbsp;url: 20,<br>&nbsp;&nbsp;Array: -21 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 22,<br>&nbsp;&nbsp;&nbsp;&nbsp;vehicles.json: 23<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -17 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 18,<br>&nbsp;&nbsp;&nbsp;&nbsp;starships.json: 19<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -14 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 15,<br>&nbsp;&nbsp;&nbsp;&nbsp;species.json: 16<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -4 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 5,<br>&nbsp;&nbsp;&nbsp;&nbsp;films.json: 6<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Films: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/swapi/input/films.json" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-2" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;created: 49,<br>&nbsp;&nbsp;director: 50,<br>&nbsp;&nbsp;edited: 51,<br>&nbsp;&nbsp;episode_id: 52,<br>&nbsp;&nbsp;opening_crawl: 53,<br>&nbsp;&nbsp;producer: 57,<br>&nbsp;&nbsp;release_date: 58,<br>&nbsp;&nbsp;title: 65,<br>&nbsp;&nbsp;url: 66,<br>&nbsp;&nbsp;Array: -67 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 68,<br>&nbsp;&nbsp;&nbsp;&nbsp;vehicles.json: 69<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -62 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 63,<br>&nbsp;&nbsp;&nbsp;&nbsp;starships.json: 64<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -59 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 60,<br>&nbsp;&nbsp;&nbsp;&nbsp;species.json: 61<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -54 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 55,<br>&nbsp;&nbsp;&nbsp;&nbsp;planets.json: 56<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -46 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 47,<br>&nbsp;&nbsp;&nbsp;&nbsp;people.json: 48<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Planets: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/swapi/input/planets.json" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-3" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;climate: 109,<br>&nbsp;&nbsp;created: 110,<br>&nbsp;&nbsp;diameter: 111,<br>&nbsp;&nbsp;edited: 112,<br>&nbsp;&nbsp;gravity: 116,<br>&nbsp;&nbsp;name: 117,<br>&nbsp;&nbsp;orbital_period: 118,<br>&nbsp;&nbsp;population: 119,<br>&nbsp;&nbsp;rotation_period: 123,<br>&nbsp;&nbsp;surface_water: 124,<br>&nbsp;&nbsp;terrain: 125,<br>&nbsp;&nbsp;url: 126,<br>&nbsp;&nbsp;Array: -120 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 121,<br>&nbsp;&nbsp;&nbsp;&nbsp;people.json: 122<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -113 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 114,<br>&nbsp;&nbsp;&nbsp;&nbsp;films.json: 115<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Species: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/swapi/input/species.json" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-4" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;average_height: 70,<br>&nbsp;&nbsp;average_lifespan: 71,<br>&nbsp;&nbsp;classification: 72,<br>&nbsp;&nbsp;created: 73,<br>&nbsp;&nbsp;designation: 74,<br>&nbsp;&nbsp;edited: 75,<br>&nbsp;&nbsp;eye_colors: 76,<br>&nbsp;&nbsp;hair_colors: 80,<br>&nbsp;&nbsp;homeworld: 81,<br>&nbsp;&nbsp;language: 82,<br>&nbsp;&nbsp;name: 83,<br>&nbsp;&nbsp;skin_colors: 87,<br>&nbsp;&nbsp;url: 88,<br>&nbsp;&nbsp;Array: -84 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 85,<br>&nbsp;&nbsp;&nbsp;&nbsp;people.json: 86<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -77 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 78,<br>&nbsp;&nbsp;&nbsp;&nbsp;films.json: 79<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Vehicles: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/swapi/input/vehicles.json" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-5" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;cargo_capacity: 89,<br>&nbsp;&nbsp;consumables: 90,<br>&nbsp;&nbsp;cost_in_credits: 91,<br>&nbsp;&nbsp;created: 92,<br>&nbsp;&nbsp;crew: 93,<br>&nbsp;&nbsp;edited: 94,<br>&nbsp;&nbsp;length: 98,<br>&nbsp;&nbsp;manufacturer: 99,<br>&nbsp;&nbsp;max_atmosphering_speed: 100,<br>&nbsp;&nbsp;model: 101,<br>&nbsp;&nbsp;name: 102,<br>&nbsp;&nbsp;passengers: 103,<br>&nbsp;&nbsp;url: 107,<br>&nbsp;&nbsp;vehicle_class: 108,<br>&nbsp;&nbsp;Array: -104 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 105,<br>&nbsp;&nbsp;&nbsp;&nbsp;people.json: 106<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -95 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 96,<br>&nbsp;&nbsp;&nbsp;&nbsp;films.json: 97<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Starships: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/swapi/input/starships.json" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-6" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;MGLT: 24,<br>&nbsp;&nbsp;cargo_capacity: 25,<br>&nbsp;&nbsp;consumables: 26,<br>&nbsp;&nbsp;cost_in_credits: 27,<br>&nbsp;&nbsp;created: 28,<br>&nbsp;&nbsp;crew: 29,<br>&nbsp;&nbsp;edited: 30,<br>&nbsp;&nbsp;hyperdrive_rating: 34,<br>&nbsp;&nbsp;length: 35,<br>&nbsp;&nbsp;manufacturer: 36,<br>&nbsp;&nbsp;max_atmosphering_speed: 37,<br>&nbsp;&nbsp;model: 38,<br>&nbsp;&nbsp;name: 39,<br>&nbsp;&nbsp;passengers: 40,<br>&nbsp;&nbsp;starship_class: 44,<br>&nbsp;&nbsp;url: 45,<br>&nbsp;&nbsp;Array: -41 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 42,<br>&nbsp;&nbsp;&nbsp;&nbsp;people.json: 43<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -31 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 32,<br>&nbsp;&nbsp;&nbsp;&nbsp;films.json: 33<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

## Generated Dataset Specifications





