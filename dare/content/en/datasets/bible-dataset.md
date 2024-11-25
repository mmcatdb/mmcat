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

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Alamo Polyglot: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/AlamoPolyglot.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-9" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;id: 95,<br>&nbsp;&nbsp;book_name: 97,<br>&nbsp;&nbsp;chapter: 98,<br>&nbsp;&nbsp;verse: 99,<br>&nbsp;&nbsp;world_english_bible_web: 100,<br>&nbsp;&nbsp;king_james_bible_kjv: 101,<br>&nbsp;&nbsp;leningrad_codex: 102,<br>&nbsp;&nbsp;jewish_publication_society_jps: 103,<br>&nbsp;&nbsp;codex_alexandrinus: 104,<br>&nbsp;&nbsp;brenton: 105,<br>&nbsp;&nbsp;samaritan_pentateuch: 106,<br>&nbsp;&nbsp;samaritan_pentateuch_english: 107,<br>&nbsp;&nbsp;onkelos_aramaic: 108,<br>&nbsp;&nbsp;onkelos_english: 109,<br>&nbsp;&nbsp;book_id: 117.18<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Book: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/Book.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-2" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;book_name: 9,<br>&nbsp;&nbsp;hebrew_name: 10,<br>&nbsp;&nbsp;hebrew_transliteration: 11,<br>&nbsp;&nbsp;hebrew_meaning: 12,<br>&nbsp;&nbsp;greek_name: 13,<br>&nbsp;&nbsp;greek_transliteration: 14,<br>&nbsp;&nbsp;greek_meaning: 15,<br>&nbsp;&nbsp;chapter_count: 16,<br>&nbsp;&nbsp;verse_count: 17,<br>&nbsp;&nbsp;book_id: 18,<br>&nbsp;&nbsp;christian_sequence: 19,<br>&nbsp;&nbsp;hebrew_sequence: 20,<br>&nbsp;&nbsp;short_name: 21,<br>&nbsp;&nbsp;usx_code: 22,<br>&nbsp;&nbsp;writer_id: 23,<br>&nbsp;&nbsp;written_start_date: 24,<br>&nbsp;&nbsp;written_end_date: 25,<br>&nbsp;&nbsp;written_location_id: 26<br>}</code></pre>" >}}  
</div>
 
<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Commandments: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/Commandments.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-8" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;commandment_number: 82,<br>&nbsp;&nbsp;commandment_concept: 83,<br>&nbsp;&nbsp;commandment_polarity: 84,<br>&nbsp;&nbsp;scripture_english: 86,<br>&nbsp;&nbsp;scripture_hebrew: 87,<br>&nbsp;&nbsp;scripture_greek: 88,<br>&nbsp;&nbsp;scripture_parashah: 89,<br>&nbsp;&nbsp;sefer_hachinuch_number: 90,<br>&nbsp;&nbsp;mishneh_torah_book_number: 91,<br>&nbsp;&nbsp;mishneh_torah_book_name: 92,<br>&nbsp;&nbsp;mishneh_torah_category: 93,<br>&nbsp;&nbsp;p119f_category: 94,<br>&nbsp;&nbsp;reference_id: 116.68<br>}</code></pre>" >}}
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Event: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/Event.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-4" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;event_id: 50,<br>&nbsp;&nbsp;event_label: 51,<br>&nbsp;&nbsp;event_description: 52,<br>&nbsp;&nbsp;event_type: 53,<br>&nbsp;&nbsp;event_year_ah: 55,<br>&nbsp;&nbsp;person_age_at_event: 56,<br>&nbsp;&nbsp;event_year_offset: 57,<br>&nbsp;&nbsp;event_reference_id: 58,<br>&nbsp;&nbsp;event_year_calculation: 59,<br>&nbsp;&nbsp;event_location: 60,<br>&nbsp;&nbsp;event_location_reference_id: 61,<br>&nbsp;&nbsp;person_id: 111.0,<br>&nbsp;&nbsp;event_notes: -62 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 63,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 64<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Hitchcock's Dictionary: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/HitchcocksBibleNamesDictionary.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-5" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;Meaning: 67,<br>&nbsp;&nbsp;english_label: 112.29<br>}</code></pre>" >}}
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Person: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/Person.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-1" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;person_id: 0,<br>&nbsp;&nbsp;person_name: 1,<br>&nbsp;&nbsp;surname: 2,<br>&nbsp;&nbsp;unique_attribute: 3,<br>&nbsp;&nbsp;sex: 4,<br>&nbsp;&nbsp;tribe: 5,<br>&nbsp;&nbsp;person_notes: 6,<br>&nbsp;&nbsp;name_instance: 7,<br>&nbsp;&nbsp;person_sequence: 8<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Person Label: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/PersonLabel.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-3" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;person_label_id: 27,<br>&nbsp;&nbsp;english_label: 29,<br>&nbsp;&nbsp;hebrew_label_transliterated: 33,<br>&nbsp;&nbsp;hebrew_strongs_number: 37,<br>&nbsp;&nbsp;greek_label_transliterated: 41,<br>&nbsp;&nbsp;greek_label_meaning: 42,<br>&nbsp;&nbsp;greek_strongs_number: 43,<br>&nbsp;&nbsp;label_reference_id: 44,<br>&nbsp;&nbsp;label_type: 45,<br>&nbsp;&nbsp;label-given_by_god: 46,<br>&nbsp;&nbsp;label_notes: 47,<br>&nbsp;&nbsp;person_label_count: 48,<br>&nbsp;&nbsp;label_sequence: 49,<br>&nbsp;&nbsp;person_id: 110.0,<br>&nbsp;&nbsp;greek_label: -38 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 39,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 40<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;hebrew_label_meaning: -34 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 35,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 36<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;hebrew_label: -30 {<br>&nbsp;&nbsp;&nbsp;&nbsp;_index: 31,<br>&nbsp;&nbsp;&nbsp;&nbsp;_value: 32<br>&nbsp;&nbsp;}<br>}</code></pre>" >}} 
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Person Relationship: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/PersonRelationship.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-7" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;person_relationship_id: 74,<br>&nbsp;&nbsp;person_relationship_sequence: 75,<br>&nbsp;&nbsp;relationship_type: 77,<br>&nbsp;&nbsp;person_id_2: 78,<br>&nbsp;&nbsp;relationship_category: 79,<br>&nbsp;&nbsp;relationship_notes: 81,<br>&nbsp;&nbsp;person_id: 114.0,<br>&nbsp;&nbsp;reference_id: 115.68<br>}</code></pre>" >}}   
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Reference: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/bible/input/Reference.csv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-6" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;reference_id: 68,<br>&nbsp;&nbsp;usx_code: 70,<br>&nbsp;&nbsp;chapter: 71,<br>&nbsp;&nbsp;verse: 72,<br>&nbsp;&nbsp;verse_sequence: 73,<br>&nbsp;&nbsp;book_id: 113.18<br>}</code></pre>" >}}   
</div>

## Generated Dataset Specifications






