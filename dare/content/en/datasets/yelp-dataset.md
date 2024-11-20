---
title: "Yelp Dataset"
weight: 0
---

The Yelp dataset includes business information, including reviews, user data, check-ins, and business attributes, offering a view of consumer interactions and feedback. 

It is structured in JSON format, with each file containing distinct data types. The files are the following:
- `business.json` with business details
- `user.json` with user profiles
- `review.json` with user reviews on businesses
- `tip.json` with advice or comments left by users about businesses
- `checkin.json` with check-in activity for businesses

This dataset was significant for our work, as we developed three distinct mappings to capture various aspects of Yelp data. Each mapping is described in detail in our paper[^article].

TODO: Change this pic

![Yelp dataset](/img/yelp-dataset.png)

## Initial Dataset Specifications

### Checkin

{{< open-link url="https://data.mmcatdb.com/bug1/checkin.json" label="Data" >}}
<br />
{{< show-image id="image1" label="Mapping" src="/img/yelp-checkin-mapping.png" alt="Initial Mapping checkin.json" >}}

## Generated multi-model data

### Example 1

### Example 2


[^article]: Holubová, I., Šrůtková, A., & Bartík. Reshaping Reality: Creating Multi-Model Data and Queries from Real-World Inputs. *The 40th ACM/SIGAPP Symposium On Applied Computing*, under review, 2024. Manuscript submitted for publication.
