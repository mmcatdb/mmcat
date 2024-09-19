---
title: "Propagation of changes to multi-model data"
---

Data can be stored in various formats (e.g., graph, document, or key-value) and databases (e.g., Neo4j, MongoDB, or Redis). Our framework allows us to access all these databases at once. For this, we use a unified, database-independent model, which is then mapped to the specific databases. However, all applications need to evolve over time, and so does the data model. We want to allow the users to propagate the changes:
- from the unified model to the mappings between it and the databases,
- from the mappings to the data stored in the databases.
The goal of the project is to implement algorithms for the propagation, as well as a GUI for the users to interact with. The theory behind this is already here - we have defined operations that can be applied to the unified model, and we have derived how they should affect the mappings and the data. The focus of the project will be on the implementation of these operations.

In many cases, the propagation will require manual input from the users. Therefore, the project can be extended to a diploma thesis, where the focus will be on automating the propagation via machine learning methods.

### Literature

1. Jáchym Bártík, Pavel Koupil, and Irena Holubová. 2024. Modelling and Evolution Management of Multi-Model Data. In Proceedings of the 39th ACM/SIGAPP Symposium on Applied Computing (SAC '24). Association for Computing Machinery, New York, NY, USA, 347–350. [https://doi.org/10.1145/3605098.3636128](https://doi.org/10.1145/3605098.3636128)
2. Pavel Koupil, Jáchym Bártík, and Irena Holubová. 2022. MM-evocat: A Tool for Modelling and Evolution Management of Multi-Model Data. In Proceedings of the 31st ACM International Conference on Information & Knowledge Management (CIKM '22). Association for Computing Machinery, New York, NY, USA, 4892–4896. [https://doi.org/10.1145/3511808.3557180](https://doi.org/10.1145/3511808.3557180)
3. Pavel Koupil, and Irena Holubová. 2022. A unified representation and transformation of multi-model data using category theory. J Big Data 9, 61 (2022). [https://doi.org/10.1186/s40537-022-00613-3](https://doi.org/10.1186/s40537-022-00613-3)

**This project is currently available.**