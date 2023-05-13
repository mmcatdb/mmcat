<script setup lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { provide, ref, shallowRef } from 'vue';
import EditorForSchemaCategory from '@/components/category/edit/EditorForSchemaCategory.vue';
import type { SchemaCategory } from '@/types/schema';
import { useRoute } from 'vue-router';
import dataspecerAPI from '@/utils/api/dataspecerAPI';
import { addImportedToGraph, importDataspecer } from '@/utils/integration';
import { toQueryScalar } from '@/utils/router';
import { evocatKey, useSchemaCategoryInfo } from '@/utils/injects';
import type { Evocat } from '@/types/evocat/Evocat';
import EvocatDisplay from '@/components/category/EvocatDisplay.vue';
import VersionsDisplay from '@/components/category/VersionsDisplay.vue';

const route = useRoute();

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();
provide(evocatKey, { evocat, graph });

async function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    graph.value = context.graph;

    const pimIri = toQueryScalar(route.query.pimIri);
    if (!pimIri)
        return;

    // Let's import the objects first so we don't have to create the schema category and then delete it immediately after if something goes wrong.
    const importResult = await dataspecerAPI.getStoreForIri(pimIri);
    if (!importResult.status)
        return;

    const importedDataspecer = importDataspecer(importResult.data);
    addImportedToGraph(importedDataspecer, context.evocat, context.graph);
}

//const graphDisplay = ref<InstanceType<typeof GraphDisplay>>();
const category = useSchemaCategoryInfo();

function schemaCategorySaved(schemaCategory: SchemaCategory) {
    graph.value = undefined;


    //graphDisplay.value?.updateSchema(schemaCategory);


    category.value = schemaCategory;
}
</script>

<template>
    <div class="divide">
        <EvocatDisplay
            @evocat-created="evocatCreated"
        />
        <div
            v-if="evocat"
        >
            <EditorForSchemaCategory
                @save="schemaCategorySaved"
            />
        </div>
    </div>
    <div v-if="evocat">
        <VersionsDisplay />
    </div>
</template>
