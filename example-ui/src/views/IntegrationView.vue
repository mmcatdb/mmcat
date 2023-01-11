<script setup lang="ts">
import { SchemaCategory, SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import { onMounted, provide, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import GraphDisplay from '@/components/category/GraphDisplay.vue';
import { categoryIdKey } from '@/utils/globalSchemaSettings';
import dataspecerAPI from '@/utils/api/dataspecerAPI';
import { addImportedToGraph, importDataspecer } from '@/utils/integration';
import type { Graph } from '@/types/categoryGraph';
import type { ImportedDataspecer } from '@/types/integration';
import EditorForSchemaCategory from '@/components/category/edit/EditorForSchemaCategory.vue';

const route = useRoute();

const error = ref('');
const categoryId = ref('');
provide(categoryIdKey, categoryId);

const schemaCategoryInfo = ref<SchemaCategoryInfo>();
const importedDataspecer = ref<ImportedDataspecer>();
const graph = ref<Graph>();

function cytoscapeCreated(newGraph: Graph) {
    if (!importedDataspecer.value)
        return;

    addImportedToGraph(importedDataspecer.value, newGraph);
    graph.value = newGraph;
}

//const graphDisplay = ref<InstanceType<typeof GraphDisplay>>();

const router = useRouter();

function schemaCategorySaved(schemaCategory: SchemaCategory) {
    graph.value = undefined;
    //graphDisplay.value?.updateSchema(schemaCategory);
    router.push({ name: 'schemaCategory', params: { categoryId: categoryId.value } });
}

onMounted(async () => {
    const pimIri = route.query.pimIri;
    const label = route.query.label;
    if (!pimIri || typeof pimIri !== 'string') {
        error.value = 'PIM IRI is not set.';
        return;
    }

    if (!label) {
        error.value = 'Schema category label is not set.';
        return;
    }

    // Let's import the objects first so we don't have to create the schema category and then delete it immediately after if something goes wrong.
    const importResult = await dataspecerAPI.getStoreForIri(pimIri);
    if (!importResult.status) {
        error.value = 'Import from dataspecer was unsuccessful.';
        return;
    }

    importedDataspecer.value = importDataspecer(importResult.data);

    // Create a schema category for the imported objects.
    const jsonValue = JSON.stringify({ label });
    const newSchemaResult = await API.schemas.createNewSchema({}, { jsonValue });
    if (!newSchemaResult.status) {
        error.value = newSchemaResult.error;
        return;
    }

    const newInfo = SchemaCategoryInfo.fromServer(newSchemaResult.data);
    categoryId.value = newInfo.id;
    schemaCategoryInfo.value = newInfo;
});
</script>

<template>
    <h1>Integration</h1>
    <p v-if="error">
        An unexpected error has occured:<br />
        <span class="text-error">{{ error }}</span>
    </p>
    <template v-if="schemaCategoryInfo">
        <Teleport to="#app-top-bar-center">
            <h2>{{ schemaCategoryInfo.label }}</h2>
        </Teleport>
        <div class="divide">
            <GraphDisplay
                ref="graphDisplay"
                @create:graph="cytoscapeCreated"
            />
            <div
                v-if="graph"
            >
                <EditorForSchemaCategory
                    :graph="graph"
                    @save="schemaCategorySaved"
                />
            </div>
        </div>
    </template>
</template>
