<script setup lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { provide, ref } from 'vue';
import GraphDisplay from '@/components/category/GraphDisplay.vue';
import EditorForSchemaCategory from '@/components/category/edit/EditorForSchemaCategory.vue';
import type { SchemaCategory } from '@/types/schema';
import { useRoute } from 'vue-router';
import dataspecerAPI from '@/utils/api/dataspecerAPI';
import { addImportedToGraph, importDataspecer } from '@/utils/integration';
import { toQueryScalar } from '@/utils/router';
import { evocatKey, useSchemaCategoryInfo } from '@/utils/injects';
import { Evocat } from '@/types/evocat/Evocat';

const route = useRoute();
const graph = ref<Graph>();
const evocat = ref<Evocat>();

provide(evocatKey, evocat);

async function cytoscapeCreated(newGraph: Graph) {
    graph.value = newGraph;
    evocat.value = Evocat.create(newGraph);

    const pimIri = toQueryScalar(route.query.pimIri);
    if (!pimIri)
        return;

    // Let's import the objects first so we don't have to create the schema category and then delete it immediately after if something goes wrong.
    const importResult = await dataspecerAPI.getStoreForIri(pimIri);
    if (!importResult.status)
        return;

    const importedDataspecer = importDataspecer(importResult.data);
    addImportedToGraph(importedDataspecer, newGraph);
}

const graphDisplay = ref<InstanceType<typeof GraphDisplay>>();
const category = useSchemaCategoryInfo();

function schemaCategorySaved(schemaCategory: SchemaCategory) {
    graph.value = undefined;
    graphDisplay.value?.updateSchema(schemaCategory);
    category.value = schemaCategory;
}
</script>

<template>
    <div class="divide">
        <GraphDisplay
            ref="graphDisplay"
            @create:graph="cytoscapeCreated"
        />
        <div
            v-if="evocat"
        >
            <EditorForSchemaCategory
                @save="schemaCategorySaved"
            />
        </div>
    </div>
</template>
