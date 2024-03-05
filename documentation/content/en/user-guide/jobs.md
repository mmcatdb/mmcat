---
title: "Jobs"
weight: 50
---

A job is a transformation algorithm applied on a [mapping](mappings.md) that can be executed by the backend application.

## Algorithms

### Model to Category

The data is imported from the database (specified in the mapping) to the instance category. If there already is an instance category, it will be expanded with the new data. If there is not any, it will be created. 

It is important to note that the instance category is bound with the session. This means that after some time the category will be automatically deleted. 

### Category to Model

This is basically an opposite process to the previous one. The algorithm creates DDL and DML statements for the specified database system according to the data in the instance category. The difference is that the statements will not be executed by the database. It will be only printed to a structure called [model](models.md).

If the instance category is empty or if it even does not exist, yet there will not be any data to export. So it is recommended to run the import jobs first.

![Job](/img/jobs.png)

## Create

To create a job, specify its name, type and select a mapping. Then click on the `Create job` button.

## Delete

Any job that is not currently running can be deleted by the `Delete` button.

## Run

Each job has a status:
- Ready - the job was not run yet.
- Running - the job is currently being executed. Its status should automatically update after some time.
- Finished - the job was finished successfully.
- Canceled - the job was not finished because an error occurred.

A job in the `Ready` status can be executed by the `Start` button. If the status is `Finished` or `Canceled` the job can be executed again by the `Restart` button. There is practically no difference between starting and restarting a job. Moreover, it so that repeated execution of the job should not have any new effect on both the instance category and the created model. Of course, this only applies if the recommended order (import jobs before the export ones) is followed.

The jobs are executed only one at a time. If several jobs are started at the same time, they will be added to a queue and executed serially.

**Lastly, it is important to note that all jobs are programmed to have a two seconds delay for testing purposes.**
