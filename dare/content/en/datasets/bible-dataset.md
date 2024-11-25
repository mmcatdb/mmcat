---
title: "BibleData Dataset"
weight: 0
---

The BibleData dataset provides a comprehensive collection of structured biblical texts and metadata, covering various translations, alongside historical texts.

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






