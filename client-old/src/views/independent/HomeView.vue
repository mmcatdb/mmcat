<script setup lang="ts">
import { ref } from 'vue';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import rawAPI from '@/utils/api/rawAPI';
import type { Workflow } from '@/types/workflow';

const DOCUMENTATION_URL = import.meta.env.VITE_DOCUMENTATION_URL;

const availableCategories = ref<SchemaCategoryInfo[]>();
const newCategoryLabel = ref('');

const EXAMPLE_SCHEMAS = [
    'basic',
    'inference',
] as const;

type ExampleSchema = typeof EXAMPLE_SCHEMAS[number];

async function fetchCategories() {
    const result = await API.schemas.getAllCategoryInfos({});
    if (!result.status)
        return false;

    availableCategories.value = result.data.map(SchemaCategoryInfo.fromServer);
    return true;
}

async function confirmNewCategory() {
    if (!availableCategories.value)
        return;

    const result = await API.schemas.createNewCategory({}, {
        label: newCategoryLabel.value,
    });
    if (!result.status)
        return;

    const newCategory = SchemaCategoryInfo.fromServer(result.data);
    availableCategories.value.push(newCategory);

    newCategoryLabel.value = '';
}

async function addExampleSchema(name: ExampleSchema) {
    await rawAPI.POST(`/example-schema/${name}`);
    fetchCategories();
}

const availableWorkflows = ref<Workflow[]>();
const newWorkflowLabel = ref('');

async function fetchWorkflows() {
    const result = await API.workflows.getAllWorkflows({});
    if (!result.status)
        return false;

    availableWorkflows.value = result.data;
    return true;
}

async function confirmNewWorkflow() {
    if (!availableWorkflows.value)
        return;

    const result = await API.workflows.createWorkflow({}, {
        label: newWorkflowLabel.value,
        type: 'inference',
    });
    if (!result.status)
        return;

    availableWorkflows.value.push(result.data);

    newWorkflowLabel.value = '';
}
</script>

<template>
    <h1>MM-cat</h1>
    <p>
        A multi-model data modeling framework based on category theory.
    </p>
    <br />
    <p>
        Detailed instructions on how to use this tool can be found <a
            :href="DOCUMENTATION_URL"
            target="_blank"
            rel="noreferrer"
        >here</a>.
    </p>
    <div class="d-flex">
        <div class="w-50">
            <h2 class="mt-3">
                Current schema categories
            </h2>
            <div
                v-if="availableCategories"
                class="schema-category-list"
            >
                <div
                    v-for="category in availableCategories"
                    :key="category.id"
                    class="schema-category-display"
                >
                    <FixedRouterLink
                        :to="{ name: 'schemaCategory', params: { categoryId: category.id } }"
                        view="category"
                    >
                        <h3>{{ category.label }}</h3>
                    </FixedRouterLink>
                </div>
            </div>
            <ResourceLoader :loading-function="fetchCategories" />
            <h2 class="mt-3">
                Create new schema category
            </h2>
            <div class="new-schema-category">
                <div class="editor mt-3">
                    <ValueContainer>
                        <ValueRow label="Label:">
                            <input v-model="newCategoryLabel" />
                        </ValueRow>
                    </ValueContainer>
                    <div class="button-row">
                        <button
                            :disabled="!newCategoryLabel || !availableCategories"
                            @click="confirmNewCategory"
                        >
                            Confirm
                        </button>
                    </div>
                </div>
            </div>
            <template v-if="EXAMPLE_SCHEMAS.length > 0">
                <h2 class="mt-3">
                    Add example schema category
                </h2>
                <div class="d-flex gap-3 py-2">
                    <button
                        v-for="schema in EXAMPLE_SCHEMAS"
                        :key="schema"
                        :onclick="() => addExampleSchema(schema)"
                    >
                        {{ schema }}
                    </button>
                </div>
            </template>
        </div>
        <div class="w-50">
            <h2 class="mt-3">
                Current workflows
            </h2>
            <div
                v-if="availableWorkflows"
                class="schema-category-list"
            >
                <div
                    v-for="workflow in availableWorkflows"
                    :key="workflow.id"
                    class="schema-category-display"
                >
                    <FixedRouterLink
                        :to="{ name: 'index', params: { workflowId: workflow.id } }"
                        view="workflow"
                    >
                        <h3>{{ workflow.label }}</h3>
                    </FixedRouterLink>
                </div>
            </div>
            <ResourceLoader :loading-function="fetchWorkflows" />
            <h2 class="mt-3">
                Create new workflow
            </h2>
            <div class="new-schema-category">
                <div class="editor mt-3">
                    <ValueContainer>
                        <ValueRow label="Label:">
                            <input v-model="newWorkflowLabel" />
                        </ValueRow>
                    </ValueContainer>
                    <div class="button-row">
                        <button
                            :disabled="!newWorkflowLabel || !availableWorkflows"
                            @click="confirmNewWorkflow"
                        >
                            Confirm
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style>
.schema-category-list {
    display: flex;
    flex-direction: column;
}

.schema-category-display {
    max-width: 400px;
    background-color: var(--color-background-mute);
    padding: 8px;
    margin-top: 12px;
    padding-left: 32px;
}

.new-schema-category {
    display: flex;
}

.editor {
    display: flex;
    flex-direction: column;
}
</style>
