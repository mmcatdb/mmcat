---
title: "Yelp Dataset"
weight: 0
---

The [Yelp dataset](https://www.yelp.com/dataset) includes business information, including reviews, user data, check-ins, and business attributes, offering a view of consumer interactions and feedback. 

It is structured in JSON format, with each file containing distinct data types. The files are the following:
- `business.json` with business details
- `user.json` with user profiles
- `review.json` with user reviews on businesses
- `tip.json` with advice or comments left by users about businesses
- `checkin.json` with check-in activity for businesses

![Yelp dataset](/img/yelp-dataset-sk.png)

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
      <td style="border: 1px solid #ddd; padding: 8px;">Business</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/yelp/input/business.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="1" file="yelp/yelp-in-business" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">User</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/yelp/input/user.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="2" file="yelp/yelp-in-user" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Review</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/yelp/input/review.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="3" file="yelp/yelp-in-review" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Tip</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/yelp/input/tip.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="4" file="yelp/yelp-in-tip" label="Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Checkin</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< open-link url="https://data.mmcatdb.com/dare/yelp/input/checkin.json" label="Data Link" >}}</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="5" file="yelp/yelp-in-checkin" label="Mapping" >}}</td>
    </tr>
  </tbody>
</table>

## Generated Dataset Specifications

### Case A: Transforming Yelp Datasets into PostgreSQL Tables

JSON files from the Yelp Datasets are transformed into structured PostgreSQL tables. Since PostgreSQL does not support nested or complex structures in its relational schema, the new mapping does not contain them. Notice, for example, the flat structure of Business and how some of the attributes were added with a composite signature, or the creation of a new kind Friends.

PostgreSQL offers several advantages for structured datasets like the Yelp Datasets. It particularly has the ability to enforce strict data integrity through constraints like primary keys and foreign keys. Furthermore, its scalability and performance allow it to handle large datasets and execute complex queries efficiently.

By storing the transformed Yelp data in PostgreSQL, the dataset becomes easier to query and analyze, providing a solid framework for extracting meaningful insights and supporting further research or development.

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Output Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Business</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="6" file="yelp/yelp-A-business" label="Output Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">User</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="7" file="yelp/yelp-A-user" label="Output Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Friends</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="8" file="yelp/yelp-A-friends" label="Output Mapping" >}}</td>
    </tr>
        <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Review</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="9" file="yelp/yelp-in-review" label="Output Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Tip</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="10" file="yelp/yelp-in-tip" label="Output Mapping" >}}</td>
    </tr>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Checkin</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="11" file="yelp/yelp-in-checkin" label="Output Mapping" >}}</td>
    </tr>
  </tbody>
</table>

<br />

Generated Data Manipulation Language (DML) Commands: {{< open-link url="https://data.mmcatdb.com/dare/yelp/output/A/commands.txt" label="Commands Link" >}}

### Case B: Embedding Tips into Business Data in MongoDB

The Yelp Business data is enriched by embedding related "tips" directly within each business as an object. Each business document in the resulting dataset contains an array of tips. This enriched structure is stored as a MongoDB collection.

By structuring the data this way, querying becomes more efficient, as all the information about a business, including its tips, can be retrieved in a single query. Additionally, MongoDB's ability to handle nested data makes it easy to access, filter, and manipulate embedded arrays, such as searching for businesses with specific tips.

This approach also reflects a more intuitive organization of the data, mirroring real-world relationships. A business inherently "owns" its tips, making embedding a logical and natural choice.

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Output Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Business</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="12" file="yelp/yelp-B-business" label="Output Mapping" >}}</td>
    </tr>
  </tbody>
</table>

<br />

Generated Data Manipulation Language (DML) Commands: {{< open-link url="https://data.mmcatdb.com/dare/yelp/output/B/commands.txt" label="Commands Link" >}}

### Case C: Transforming Tips into a CSV File

The Yelp Tip data is transformed into a flat CSV file. Each row in the CSV file represents an individual tip. This straightforward structure provides a compact and highly portable format that can be easily used across a variety of tools and workflows.

<table style="width: 100%; border-collapse: collapse; text-align: left;">
  <thead>
    <tr>
      <th style="border: 1px solid #ddd; padding: 8px;">Entity</th>
      <th style="border: 1px solid #ddd; padding: 8px;">Output Mapping</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="border: 1px solid #ddd; padding: 8px;">Business</td>
      <td style="border: 1px solid #ddd; padding: 8px;">{{< show-code id="13" file="yelp/yelp-in-tip" label="Output Mapping" >}}</td>
    </tr>
  </tbody>
</table>

<br />

Generated File: {{< open-link url="https://data.mmcatdb.com/dare/yelp/output/C/file.csv" label="File Link" >}}
