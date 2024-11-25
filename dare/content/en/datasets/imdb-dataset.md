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
