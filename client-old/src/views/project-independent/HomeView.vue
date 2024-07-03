<script setup lang="ts">
import { ref } from 'vue';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import rawAPI from '@/utils/api/rawAPI';

const DOCUMENTATION_URL = import.meta.env.VITE_DOCUMENTATION_URL;

const avaliableCategories = ref<SchemaCategoryInfo[]>();
const newCategoryLabel = ref('');

const EXAMPLE_SCHEMAS = [
    'basic',
] as const;

type ExampleSchema = typeof EXAMPLE_SCHEMAS[number];

async function fetchCategories() {
    const result = await API.schemas.getAllCategoryInfos({});
    if (!result.status)
        return false;

    avaliableCategories.value = result.data.map(SchemaCategoryInfo.fromServer);
    return true;
}

async function confirmNewCategory() {
    if (!avaliableCategories.value)
        return;

    const result = await API.schemas.createNewCategory({}, {
        label: newCategoryLabel.value,
    });
    if (!result.status)
        return;

    const newCategory = SchemaCategoryInfo.fromServer(result.data);
    avaliableCategories.value.push(newCategory);

    newCategoryLabel.value = '';
}

async function addExampleSchema(name: ExampleSchema) {
    await rawAPI.POST(`/example-schema/${name}`);
    fetchCategories();
}
</script>

<template>
    <h1>MM-cat</h1>
    <p>
        A multi-model data modeling framework based on category theory.
    </p>
    <br />
    <p>
        Detailed instructions on how to use this tool can be found <a :href="DOCUMENTATION_URL">here</a>.
    </p>
    <h2 class="mt-3">
        Current schema categories
    </h2>
    <div
        v-if="avaliableCategories"
        class="schema-category-list"
    >
        <div
            v-for="category in avaliableCategories"
            :key="category.id"
            class="schema-category-display"
        >
            <CleverRouterLink
                :to="{ name: 'schemaCategory', params: { categoryId: category.id } }"
            >
                <h3>{{ category.label }}</h3>
            </CleverRouterLink>
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
                    :disabled="!newCategoryLabel || !avaliableCategories"
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
