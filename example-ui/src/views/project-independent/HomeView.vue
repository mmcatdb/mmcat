<script setup lang="ts">
import { ref } from 'vue';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import CleverRouterLink from '@/components/CleverRouterLink.vue';
import ResourceLoader from '@/components/ResourceLoader.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

const DOCUMENTATION_URL = import.meta.env.VITE_DOCUMENTATION_URL;

const avaliableCategories = ref<SchemaCategoryInfo[]>();
const newCategoryLabel = ref('');

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

    const jsonValue = JSON.stringify({ label: newCategoryLabel.value });
    const result = await API.schemas.createNewSchema({}, { jsonValue });
    if (!result.status)
        return;

    const newCategory = SchemaCategoryInfo.fromServer(result.data);
    avaliableCategories.value.push(newCategory);

    newCategoryLabel.value = '';
}
</script>

<template>
    <h1>MM-evocat</h1>
    <p>
        A multi-model data modelling framework based on category theory.
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
