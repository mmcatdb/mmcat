---
title: "Generation Workflow"
weight: 30
---

We have introduced workflows to guide users through various use cases. The Generation Workflow offers step-by-step directions through the process of generating multi-model data from a single model input. It encompasses the complete process, including the inference of a schema from raw data, editing the inferred schema category, creating mappings, and generating the multi-model data itself. Following steps will guide you through the process:

## Step 0
The Home page of the mmcat client presents the user with multiple options. To start the workflow process, fill in the workflow label and press "Confirm".

![Workflow Creation](/img/new-workflow.png)

## Step 1
Select or create a new Datasource. When creating new, fill in the required information. Press "Continue".

![Workflow Input Selection](/img/select-input.png)

## Step 2
The Inference Job has run. If succesful, the state badge will change its state to "Waiting". Edit the inferred Schema Category to your liking. 

Define the edit and see a live preview of it by pressing "Confirm". To keep the change press "Save", otherwise press "Cancel". All of the saved edits stay available in the editor, you can choose to undo or redo them any time. Once your done with editing press "Save and Finish" followed by "Continue". 

![Workflow Schema Editing](/img/edit-schema.png)

## Step 3
Define at least one output Mapping. First, select the output Datasource. Then define the Mappings. You can either load an initial Mapping or create a new one from scratch. Both options enable you to edit the Mapping as you go using the context menu or the keyboard shortcuts. Press "Finish Mapping" when your done defining your output Mappings. To see the results press "Continue".

![Workflow Mapping Addition](/img/add-mappings.png)

## Step 4
Depending on the type of the output Datasource, you will see either Data Manipulation Language (DML) commands or generated files.

![Workflow Result View](/img/view-results.png)

## Step 5
Enjoy your generated multi-model dataset!

