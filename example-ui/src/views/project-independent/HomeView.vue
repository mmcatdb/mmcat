<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import CleverRouterLink from '@/components/CleverRouterLink.vue';

const DOCUMENTATION_URL = import.meta.env.VITE_DOCUMENTATION_URL;

const selectedSchema = ref<SchemaCategoryInfo>();
const currentSchema = ref<SchemaCategoryInfo>();
const avaliableCategories = ref<SchemaCategoryInfo[]>([]);
const newSchemaLabel = ref('');

onMounted(async () => {
    const result = await API.schemas.getAllCategoryInfos({});
    if (!result.status)
        return;

    avaliableCategories.value = result.data.map(SchemaCategoryInfo.fromServer);
    //const currentId = inject<number>('schemaCategoryId') || 2;
    const currentId = 2;
    console.log('currentId: ' + currentId);
    currentSchema.value = avaliableCategories.value.find(schema => schema.id === currentId);
});

function confirmNewId() {
    if (!selectedSchema.value)
        return;

    currentSchema.value = selectedSchema.value;
    selectedSchema.value = undefined;
}

async function confirmNewSchema() {
    const jsonValue = JSON.stringify({ label: newSchemaLabel.value });
    const result = await API.schemas.createNewSchema({}, { jsonValue });
    if (!result.status)
        return;

    const newSchema = SchemaCategoryInfo.fromServer(result.data);
    avaliableCategories.value.push(newSchema);

    newSchemaLabel.value = '';

    currentSchema.value = newSchema;
    selectedSchema.value = undefined;
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
    <div class="schema-category-list">
        <div
            v-for="category in avaliableCategories"
            :key="category.id"
            class="schema-category-display"
        >
            <CleverRouterLink
                :to="{ name: 'schemaCategory', params: { schemaCategoryId: category.id } }"
            >
                <h3>{{ category.label }}</h3>
            </CleverRouterLink>
        </div>
    </div>
    <h2 class="mt-3">
        Create new schema category
    </h2>
    <div class="new-schema-category">
        <div class="editor mt-3">
            <table>
                <tr>
                    <td class="label">
                        Label:
                    </td>
                    <td class="value">
                        <input v-model="newSchemaLabel" />
                    </td>
                </tr>
                <tr>&nbsp;</tr>
            </table>
            <div class="button-row">
                <button
                    :disabled="!newSchemaLabel"
                    @click="confirmNewSchema"
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
