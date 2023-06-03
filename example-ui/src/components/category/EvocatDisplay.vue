<script setup lang="ts">
import { onMounted, ref, shallowRef } from 'vue';
import API from '@/utils/api';
import { SchemaCategory } from '@/types/schema';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import type { Graph } from '@/types/categoryGraph';
import { useSchemaCategoryId, useSchemaCategoryInfo } from '@/utils/injects';
import GraphDisplay from './GraphDisplay.vue';
import { Evocat } from '@/types/evocat/Evocat';
import { LogicalModel } from '@/types/logicalModel';
import { DataResultSuccess } from '@/types/api/result';
import { SchemaUpdate, type SchemaUpdateInit } from '@/types/schema/SchemaUpdate';

const categoryId = useSchemaCategoryId();

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();
const fetching = ref(true);

const emit = defineEmits([ 'evocatCreated' ]);

onMounted(async () => {
    const schemaCategoryResult = await API.schemas.getCategoryWrapper({ id: categoryId });
    const schemaUpdatesResult = await API.schemas.getCategoryUpdates({ id: categoryId });
    const logicalModelsResult = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    fetching.value = false;

    if (!schemaCategoryResult.status || !schemaUpdatesResult.status || !logicalModelsResult.status) {
        // TODO handle error
        return;
    }

    const schemaUpdates = schemaUpdatesResult.data.map(SchemaUpdate.fromServer);
    const logicalModels = logicalModelsResult.data.map(LogicalModel.fromServer);

    const schemaCategory = SchemaCategory.fromServer(schemaCategoryResult.data, logicalModels);
    const newEvocat = Evocat.create(schemaCategory, schemaUpdates, logicalModels, {
        update: updateFunction,
    });
    evocat.value = newEvocat;

    if (graph.value)
        contextCompleted(evocat.value, graph.value);

});

const info = useSchemaCategoryInfo();

async function updateFunction(update: SchemaUpdateInit, models: LogicalModel[]) {
    fetching.value = true;
    const result = await API.schemas.updateCategoryWrapper({ id: categoryId }, update);
    fetching.value = false;

    if (!result.status) {
        // TODO handle error
        console.log('Update failed');
        return result;
    }

    const schemaCategory = SchemaCategory.fromServer(result.data, models);
    info.value = schemaCategory;

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
