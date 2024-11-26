---
title: "IMDb Dataset"
weight: 0
---

The IMDb dataset is a comprehensive collection of information about movies, TV shows, and the entertainment industry. It includes metadata such as titles, genres, release dates, ratings, cast and crew details, and user reviews.

It is structured in TSV format including the following files:
- `name.basics.tsv` with information about individuals in the industry
- `title.akas.tsv` with alternative title for movies and TV shows
- `title.basics.tsv` with fundamental details about titles
- `title.crew` with lists of directors and writers for each title
- `title.episode.tsv` with details for episodes of TV series
- `title.principals.tsv` with principal cast and crew for each title
- `title.ratings.tsv` with user ratings for titles

![IMDb dataset](/img/imdb-dataset-sk.png)

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
      <td style="border: 1px solid #ddd; padding: 8px;">Name.basics</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/imdb/input/name.basics.tsv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="1" file="imdb/imdb-in-name" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.akas</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.akas.tsv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="2" file="imdb/imdb-in-akas" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.basics</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.basics.tsv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="3" file="imdb/imdb-in-basics" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.crew</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.crew.tsv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="4" file="imdb/imdb-in-crew" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.episode</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.episode.tsv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="5" file="imdb/imdb-in-episode" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.principals</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.principals.tsv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="6" file="imdb/imdb-in-principals" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.ratings</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.ratings.tsv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="7" file="imdb/imdb-in-ratings" label="Mapping" >}}</td>
    </tr>
  </tbody>
</table>

## Generated Dataset Specifications

### Case A: Transforming Title Data into Embedded JSON

The title.basics table, which contains information about movies, TV shows, and other titles, is enriched by embedding additional data from the title.akas table. The title.akas table provides alternative titles for the same content, often specific to regions or languages. By embedding this data directly into each title's JSON representation, the result is a unified, self-contained dataset where each title includes its own set of alternative titles.

Transforming the original TSV files into JSON with embedded data offers several benefits. Firstly, by embedding the alternative titles directly into each title from title.basics, the resulting JSON structure becomes self-contained - all information about a title is available in one place.Secondly, JSON is human-readable and easier to work with compared to TSV files, especially when dealing with hierarchical or nested data. And finally, the original TSV files separate title.basics and title.akas into different datasets, which can lead to redundant processing steps to combine them during analysis. JSON embedding simplifies this by consolidating the data at the storage stage.

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Output Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.basics</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="8" file="imdb/imdb-A-basics" label="Output Mapping" >}}</td>
    </tr>
  </tbody>
</table>

<br />

Generated File: {{< open-link url="https://data.mmcatdb.com/dare/imdb/output/A/file.json" label="File Link" >}}

### Case B: Splitting Title Crew Data into Separate CSV Files for Directors and Writers

The title.crew data, which originally combined both directors and writers into a single file, is split into two separate CSV files. This separation ensures that each CSV file serves a more focused and specific purpose.

Having separate files allows users to work directly with the subset of data they need, reducing the preprocessing steps required to extract relevant information.

Currently, the tool can perform this splitting operation, but the process is somewhat inefficient. The tool duplicates the entire title.crew data and then removes directors from one file and writers from the other. While this method achieves the desired result, it is not optimal in terms of processing efficiency. To address this limitation, we plan to implement **conditional mapping**, a feature that will allow selective processing of data based on specific conditions. This feature is part of our planned **future work** and will make operations like this significantly faster and more efficient.

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Output Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.crew.writers</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="9" file="imdb/imdb-B-writers" label="Output Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Title.crew.directors</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="9" file="imdb/imdb-B-directors" label="Output Mapping" >}}</td>
    </tr>
  </tbody>
</table>

<br />

Generated Files: {{< open-link url="https://data.mmcatdb.com/dare/imdb/output/B/writers.csv" label="Writers File Link" >}}  {{< open-link url="https://data.mmcatdb.com/dare/imdb/output/B/directors.csv" label="Directors File Link" >}}
