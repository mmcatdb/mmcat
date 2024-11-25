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

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Name.basics: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/name.basics.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-1" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;nconst: 28,<br>&nbsp;&nbsp;primaryName: 29,<br>&nbsp;&nbsp;birthYear: 30,<br>&nbsp;&nbsp;deathYear: 31,<br>&nbsp;&nbsp;knownForTitles: -35 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 36,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 37<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;primaryProfession: -32 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 33,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 34<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>


<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.akas: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.akas.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
    {{< show-code id="code-block-2" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;ordering: 4,<br>&nbsp;&nbsp;title: 5,<br>&nbsp;&nbsp;region: 6,<br>&nbsp;&nbsp;language: 7,<br>&nbsp;&nbsp;attributes: 11,<br>&nbsp;&nbsp;isOriginalTitle: 12,<br>&nbsp;&nbsp;tconst: 54.13,<br>&nbsp;&nbsp;types: -8 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 9,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 10<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.basics: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.basics.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-3" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;tconst: 13,<br>&nbsp;&nbsp;titleType: 14,<br>&nbsp;&nbsp;primaryTitle: 15,<br>&nbsp;&nbsp;originalTitle: 16,<br>&nbsp;&nbsp;isAdult: 17,<br>&nbsp;&nbsp;startYear: 18,<br>&nbsp;&nbsp;endYear: 19,<br>&nbsp;&nbsp;runtimeMinutes: 20,<br>&nbsp;&nbsp;genres: -21 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 22,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 23<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.crew: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.crew.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-4" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;tconst: 58.13,<br>&nbsp;&nbsp;Array: -50 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 51,<br>&nbsp;&nbsp;&nbsp;&nbsp;name.basics.tsv: 52<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;Array: -47 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 48,<br>&nbsp;&nbsp;&nbsp;&nbsp;name.basics.tsv: 49<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>


<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.episode: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.episode.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-5" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;parentTconst: 25,<br>&nbsp;&nbsp;seasonNumber: 26,<br>&nbsp;&nbsp;episodeNumber: 27,<br>&nbsp;&nbsp;tconst: 57.13<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.principals: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.principals.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-6" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;ordering: 39,<br>&nbsp;&nbsp;category: 41,<br>&nbsp;&nbsp;job: 42,<br>&nbsp;&nbsp;tconst: 55.13,<br>&nbsp;&nbsp;nconst: 56.28,<br>&nbsp;&nbsp;characters: -43 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 44,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 45<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.ratings: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.ratings.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-7" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;averageRating: 1,<br>&nbsp;&nbsp;numVotes: 2,<br>&nbsp;&nbsp;tconst: 53.13<br>}</code></pre>" >}}  
</div>

## Generated Dataset Specifications
