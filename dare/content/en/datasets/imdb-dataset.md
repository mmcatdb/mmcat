---
title: "IMDb Dataset"
weight: 0
---

The IMDb dataset is a comprehensive collection of information about movies, TV shows, and the entertainment industry. It includes metadata such as titles, genres, release dates, ratings, cast and crew details, and user reviews.

It is structure in TSV format including the following files:
- `name.basics.tsv` with information about individuals in the industry
- `title.akas.tsv` with alternative title for movies and TV shows
- `title.basics.tsv` with fundamental details about titles
- `title.crew` with lists of directors and writers for each title
- `title.episode.tsv` with details for episodes of TV series
- `title.principals.tsv` with principal cast and crew for each title
- `title.ratings.tsv` with user ratings for titles

## Initial Dataset Specifications

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Name.basics: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/name.basics.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-1" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;address: 0,<br>&nbsp;&nbsp;attributes: 1,<br>&nbsp;&nbsp;business_id: 42,<br>&nbsp;&nbsp;categories: 43,<br>&nbsp;&nbsp;city: 44,<br>&nbsp;&nbsp;hours: 45,<br>&nbsp;&nbsp;is_open: 54,<br>&nbsp;&nbsp;latitude: 55,<br>&nbsp;&nbsp;longitude: 56,<br>&nbsp;&nbsp;name: 57,<br>&nbsp;&nbsp;postal_code: 58,<br>&nbsp;&nbsp;review_count: 59,<br>&nbsp;&nbsp;stars: 60,<br>&nbsp;&nbsp;state: 61,<br>&nbsp;&nbsp;attributes: 2 {<br>&nbsp;&nbsp;&nbsp;&nbsp;AcceptsInsurance: 3,<br>&nbsp;&nbsp;&nbsp;&nbsp;AgesAllowed: 4,<br>&nbsp;&nbsp;&nbsp;&nbsp;Alcohol: 5,<br>&nbsp;&nbsp;&nbsp;&nbsp;Ambience: 6,<br>&nbsp;&nbsp;&nbsp;&nbsp;BYOB: 7,<br>&nbsp;&nbsp;&nbsp;&nbsp;BYOBCorkage: 8,<br>&nbsp;&nbsp;&nbsp;&nbsp;BestNights: 9,<br>&nbsp;&nbsp;&nbsp;&nbsp;BikeParking: 10,<br>&nbsp;&nbsp;&nbsp;&nbsp;BusinessAcceptsBitcoin: 11,<br>&nbsp;&nbsp;&nbsp;&nbsp;BusinessAcceptsCreditCards: 12,<br>&nbsp;&nbsp;&nbsp;&nbsp;BusinessParking: 13,<br>&nbsp;&nbsp;&nbsp;&nbsp;ByAppointmentOnly: 14,<br>&nbsp;&nbsp;&nbsp;&nbsp;Caters: 15,<br>&nbsp;&nbsp;&nbsp;&nbsp;CoatCheck: 16,<br>&nbsp;&nbsp;&nbsp;&nbsp;Corkage: 17,<br>&nbsp;&nbsp;&nbsp;&nbsp;DietaryRestrictions: 18,<br>&nbsp;&nbsp;&nbsp;&nbsp;DogsAllowed: 19,<br>&nbsp;&nbsp;&nbsp;&nbsp;DriveThru: 20,<br>&nbsp;&nbsp;&nbsp;&nbsp;GoodForDancing: 21,<br>&nbsp;&nbsp;&nbsp;&nbsp;GoodForKids: 22,<br>&nbsp;&nbsp;&nbsp;&nbsp;GoodForMeal: 23,<br>&nbsp;&nbsp;&nbsp;&nbsp;HairSpecializesIn: 24,<br>&nbsp;&nbsp;&nbsp;&nbsp;HappyHour: 25,<br>&nbsp;&nbsp;&nbsp;&nbsp;HasTV: 26,<br>&nbsp;&nbsp;&nbsp;&nbsp;Music: 27,<br>&nbsp;&nbsp;&nbsp;&nbsp;NoiseLevel: 28,<br>&nbsp;&nbsp;&nbsp;&nbsp;Open24Hours: 29,<br>&nbsp;&nbsp;&nbsp;&nbsp;OutdoorSeating: 30,<br>&nbsp;&nbsp;&nbsp;&nbsp;RestaurantsAttire: 31,<br>&nbsp;&nbsp;&nbsp;&nbsp;RestaurantsCounterService: 32,<br>&nbsp;&nbsp;&nbsp;&nbsp;RestaurantsDelivery: 33,<br>&nbsp;&nbsp;&nbsp;&nbsp;RestaurantsGoodForGroups: 34,<br>&nbsp;&nbsp;&nbsp;&nbsp;RestaurantsPriceRange2: 35,<br>&nbsp;&nbsp;&nbsp;&nbsp;RestaurantsReservations: 36,<br>&nbsp;&nbsp;&nbsp;&nbsp;RestaurantsTableService: 37,<br>&nbsp;&nbsp;&nbsp;&nbsp;RestaurantsTakeOut: 38,<br>&nbsp;&nbsp;&nbsp;&nbsp;Smoking: 39,<br>&nbsp;&nbsp;&nbsp;&nbsp;WheelchairAccessible: 40,<br>&nbsp;&nbsp;&nbsp;&nbsp;WiFi: 41<br>&nbsp;&nbsp;},<br>&nbsp;&nbsp;hours: 4 {<br>&nbsp;&nbsp;&nbsp;&nbsp;Friday: 47,<br>&nbsp;&nbsp;&nbsp;&nbsp;Monday: 48,<br>&nbsp;&nbsp;&nbsp;&nbsp;Saturday: 49,<br>&nbsp;&nbsp;&nbsp;&nbsp;Sunday: 50,<br>&nbsp;&nbsp;&nbsp;&nbsp;Thursday: 51,<br>&nbsp;&nbsp;&nbsp;&nbsp;Tuesday: 52,<br>&nbsp;&nbsp;&nbsp;&nbsp;Wednesday: 53<br>&nbsp;&nbsp;}<br>}</code></pre>" >}}  
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.akas: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.akas.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-2" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;average_stars: 62,<br>&nbsp;&nbsp;compliment_cool: 63,<br>&nbsp;&nbsp;compliment_cute: 64,<br>&nbsp;&nbsp;compliment_funny: 65,<br>&nbsp;&nbsp;compliment_hot: 66,<br>&nbsp;&nbsp;compliment_list: 67,<br>&nbsp;&nbsp;compliment_more: 68,<br>&nbsp;&nbsp;compliment_note: 69,<br>&nbsp;&nbsp;compliment_photos: 70,<br>&nbsp;&nbsp;compliment_plain: 71,<br>&nbsp;&nbsp;compliment_profile: 72,<br>&nbsp;&nbsp;compliment_writer: 73,<br>&nbsp;&nbsp;cool: 74,<br>&nbsp;&nbsp;elite: 75,<br>&nbsp;&nbsp;fans: 76,<br>&nbsp;&nbsp;friends: 77,<br>&nbsp;&nbsp;funny: 78,<br>&nbsp;&nbsp;name: 79,<br>&nbsp;&nbsp;review_count: 80,<br>&nbsp;&nbsp;useful: 81,<br>&nbsp;&nbsp;user_id: 82,<br>&nbsp;&nbsp;yelping_since: 83<br>}</code></pre>" >}}
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.basics: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.basics.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-3" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;cool: 85,<br>&nbsp;&nbsp;date: 86,<br>&nbsp;&nbsp;funny: 87,<br>&nbsp;&nbsp;review_id: 88,<br>&nbsp;&nbsp;stars: 89,<br>&nbsp;&nbsp;text: 90,<br>&nbsp;&nbsp;useful: 91,<br>&nbsp;&nbsp;business_id: 100.42,<br>&nbsp;&nbsp;user_id: 101.82<br>}</code></pre>" >}}
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.crew: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.crew.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-4" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;compliment_count: 94,<br>&nbsp;&nbsp;date: 95,<br>&nbsp;&nbsp;text: 96,<br>&nbsp;&nbsp;business_id: 102.42,<br>&nbsp;&nbsp;user_id: 103.82<br>}</code></pre>" >}}
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.episode: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.episode.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-5" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;date: 99,<br>&nbsp;&nbsp;business_id: 104.42<br>}</code></pre>" >}}
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.principals: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.principals.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-6" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;date: 99,<br>&nbsp;&nbsp;business_id: 104.42<br>}</code></pre>" >}}
</div>

<div style="display: flex; align-items: center; gap: 10px; margin-bottom: 10px;">
  <h4 style="margin: 0;">Title.ratings: </h4>
  {{< open-link url="https://data.mmcatdb.com/dare/imdb/input/title.ratings.tsv" label="Data Link" >}}
    <span style="margin: 0;"></span>
  {{< show-code id="code-block-7" label="Mapping" code="<pre><code>_: {<br>&nbsp;&nbsp;date: 99,<br>&nbsp;&nbsp;business_id: 104.42<br>}</code></pre>" >}}
</div>