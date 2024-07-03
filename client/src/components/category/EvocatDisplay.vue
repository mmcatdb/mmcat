<script setup lang="ts">
import { onMounted, ref, shallowRef } from 'vue';
import API from '@/utils/api';
import { SchemaCategory } from '@/types/schema';

import ResourceNotFound from '@/components/common/ResourceNotFound.vue';
import type { Graph } from '@/types/categoryGraph';
import { useSchemaCategoryId, useSchemaCategoryInfo } from '@/utils/injects';
import GraphDisplay from './GraphDisplay.vue';
import { Evocat } from '@/types/evocat/Evocat';
import { LogicalModel } from '@/types/logicalModel';
import { DataResultSuccess } from '@/types/api/result';
import { SchemaUpdate, type MetadataUpdate, type SchemaUpdateInit } from '@/types/schema/SchemaUpdate';

const props = defineProps<{
    schemaCategory?: SchemaCategory;
}>();

const categoryId = useSchemaCategoryId();

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();
const fetching = ref(true);

const emit = defineEmits([ 'evocatCreated' ]);

// TODO: for now I have added this work around so that I can pass a SchemaCategory to EvocatDisplay and see it in my Job Editor
// but make sure to later create your own display for the editor and get rid of this

async function fetchData() {
    if (props.schemaCategory) {
        initializeEvocat(props.schemaCategory);
        return;
    }
    
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
    initializeEvocat(schemaCategory, schemaUpdates, logicalModels);
}

function initializeEvocat(schemaCategory: SchemaCategory, schemaUpdates: SchemaUpdate[] = [], logicalModels: LogicalModel[] = []) {
    const newEvocat = Evocat.create(schemaCategory, schemaUpdates, logicalModels, {
        update: updateFunction,
        updateMetadata: updateMetadataFunction,
    });
    evocat.value = newEvocat;

    if (graph.value)
        contextCompleted(evocat.value, graph.value);
}

onMounted(fetchData);

/*
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
        updateMetadata: updateMetadataFunction,
    });
    evocat.value = newEvocat;

    if (graph.value)
        contextCompleted(evocat.value, graph.value);

});
*/
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

async function updateMetadataFunction(metadata: MetadataUpdate[]) {
    fetching.value = true;
    const result = await API.schemas.updateCategoryMetadata({ id: categoryId }, metadata);
    fetching.value = false;

    if (!result.status)
        console.log('Update metadata failed');
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

async function updatePositions() {
    await evocat.value?.updateMetadata();
}
</script>

<template>
    <GraphDisplay
        v-if="!!evocat || fetching"
        @graph-created="graphCreated"
        @update-positions="updatePositions"
    />
    <ResourceNotFound v-else />
</template>
