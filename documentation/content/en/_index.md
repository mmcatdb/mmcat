---
title: "MM-cat"
---

*A multi-model data modelling framework based on category theory.*

## Motivation

There are lot of different database systems and each have different advantages and disadvantages. Although they might appear to be similar, there are no standards between them that would allow us to use them interchangeably. There also isn't any universal way how to transform data from one system to another or how to query among data from more than one database model. Moreover, this lack of standardization makes any effort for a general evolution tool almost impossible.

The MM-cat framewok attempts to solve this issue by creating a platform independent conceptual model as an abstract layer above all the platform dependent logical models. This solution, which is based on the [category theory](theoreticalBackground/categoryTheory.md), was theoretically developed by the article [1].

## Get Started

Please try the demo of the [Application](http://nosql.ms.mff.cuni.cz/mmcat/). Its codebase is hosted on [GitLab](https://gitlab.mff.cuni.cz/contosp/evolution-management). There you can also find all the necessary information how to build and deploy the application.

This documentation was created by [Hugo](https://gohugo.io/) with the [Geekdocs](https://geekdocs.de/) theme. Its codebase is also on [GitLab](https://gitlab.mff.cuni.cz/bartikj3/mmcat-docs/).

---

[1] Koupil, P., Holubová, I. A unified representation and transformation of multi-model data using category theory. *J Big Data* **9**, 61 (2022). https://doi.org/10.1186/s40537-022-00613-3
