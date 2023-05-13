<script setup lang="ts">
import { onMounted, ref, shallowRef } from 'vue';
import API from '@/utils/api';
import { SchemaCategory, type SchemaCategoryUpdate } from '@/types/schema';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import type { Graph } from '@/types/categoryGraph';
import { useSchemaCategoryId } from '@/utils/injects';
import GraphDisplay from './GraphDisplay.vue';
import { Evocat } from '@/types/evocat/Evocat';
import { LogicalModel } from '@/types/logicalModel';
import { DataResultSuccess } from '@/types/api/result';

const categoryId = useSchemaCategoryId();

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();
const fetching = ref(true);

const emit = defineEmits([ 'evocatCreated' ]);

onMounted(async () => {
    const schemaCategoryResult = await API.schemas.getCategoryWrapper({ id: categoryId });
    const logicalModelsResult = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    fetching.value = false;

    if (!schemaCategoryResult.status || !logicalModelsResult.status) {
        // TODO handle error
        return;
    }

    const schemaCategory = SchemaCategory.fromServer(schemaCategoryResult.data);
    const logicalModels = logicalModelsResult.data.map(LogicalModel.fromServer);

    const newEvocat = Evocat.create(schemaCategory, logicalModels, {
        update: updateFunction,
    });
    evocat.value = newEvocat;

    if (graph.value)
        contextCompleted(evocat.value, graph.value);

});

async function updateFunction(update: SchemaCategoryUpdate) {
    fetching.value = true;
    const result = await API.schemas.updateCategoryWrapper({ id: categoryId }, update);
    fetching.value = false;

    if (!result.status) {
        // TODO handle error
        console.log('Update failed');
        return result;
    }

    const schemaCategory = SchemaCategory.fromServer(result.data);
    return DataResultSuccess(schemaCategory);
}

function graphCreated(newGraph: Graph) {
    graph.value = newGraph;
    if (evocat.value)
        contextCompleted(evocat.value, graph.value);

}

function contextCompleted(evocat: Evocat, graph: Graph) {
    evocat.graph = graph;
    emit('evocatCreated', { evocat, graph });
}
</script>

<template>
    <GraphDisplay
        v-if="!!evocat || fetching"
        @graph-created="graphCreated"
    />
    <ResourceNotFound v-else />
</template>
