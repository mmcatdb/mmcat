---
title: "BibleData Dataset"
weight: 0
---

The [BibleData dataset](https://www.kaggle.com/datasets/bradystephenson/bibledata) provides a comprehensive collection of structured biblical texts and metadata, covering various translations, alongside historical texts.

The whole dataset contains 18 CSV files. To maintain readability of the inferred Schema Category and overall clarity we have worked with these 9 files:
- `AlamoPolyglot.csv` with plain-text data as well as metadata entries for the Bible manuscripts
- `Book.csv` with books of the Bible and their details
- `Commandments.csv` with basic information on the biblical commandments
- `Event.csv` with specifics of the events described
- `HitchcocksBibleNamesDictionary.csv` with Roswell D. Hitchcock's Bible Names Dictionary
- `Person.csv` with information about each named individual in the Bible
- `PersonLabel.csv` with detailed information about individual's labels
- `PersonRelationship.csv` with details on the relationships among individuals
- `Reference.csv` - with unique identifiers for each book, chapter and verse in the Bible

![BibleData dataset](/img/bible-dataset-sk.png)

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
      <td style="border: 1px solid #ddd; padding: 8px;">Alamo Polyglot</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/AlamoPolyglot.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="1" file="bible/bible-in-alamo" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Book</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/Book.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="2" file="bible/bible-in-book" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Commandments</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/Commandments.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="3" file="bible/bible-in-commandments" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Event</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/Event.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="4" file="bible/bible-in-event" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Hitchcock's Dictionary</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/HitchcocksBibleNamesDictionary.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="5" file="bible/bible-in-hitchcock" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Person</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/Person.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="6" file="bible/bible-in-person" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Person Label</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/PersonLabel.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="7" file="bible/bible-in-label" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Person Relationship</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/PersonRelationship.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="8" file="bible/bible-in-relationship" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Reference</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/bible/input/Reference.csv" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="9" file="bible/bible-in-reference" label="Mapping" >}}</td>
    </tr>
  </tbody>
</table>


## Generated Dataset Specifications

### Case A: Transforming Person Relationships and Labels into Neo4j

The Person, PersonLabel, and PersonRelationship data are transformed into a graph structure and stored in Neo4j, a graph database. The original data contains relational information about biblical figures, and their relationships with one another. This inherent graph-like structure makes Neo4j an ideal choice for storing and analyzing this data.

The relationships in the dataset form a natural graph structure, with people as nodes and their connections as edges. Neo4j is specifically designed to store, query, and analyze such graph data efficiently.

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Output Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Person</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="10" file="bible/bible-A-person" label="Output Mapping" >}}</td>
    </tr>
        <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">PersonLabel</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="11" file="bible/bible-A-label" label="Output Mapping" >}}</td>
    </tr>
        <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">PersonRelationship</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="12" file="bible/bible-A-relationship" label="Output Mapping" >}}</td>
    </tr>
  </tbody>
</table>

<br />

Generated Data Manipulation Language (DML) Commands: {{< open-link url="https://data.mmcatdb.com/dare/bible/output/A/commands.txt" label="Commands Link" >}}







