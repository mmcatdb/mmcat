<script setup lang="ts">
import { onMounted, ref, shallowRef } from 'vue';
import API from '@/utils/api';
import { Category } from '@/types/schema';
import ResourceNotFound from '@/components/common/ResourceNotFound.vue';
import type { Graph } from '@/types/categoryGraph';
import { useSchemaCategoryId, useSchemaCategoryInfo } from '@/utils/injects';
import GraphDisplay from './GraphDisplay.vue';
import { Evocat } from '@/types/evocat/Evocat';
import { DataResultSuccess } from '@/types/api/result';
import { SchemaUpdate, type SchemaUpdateInit } from '@/types/schema/SchemaUpdate';
import { type LogicalModel, logicalModelsFromServer } from '@/types/datasource';

const props = defineProps<{
    schemaCategory?: Category;
}>();

const categoryId = useSchemaCategoryId();

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();
const fetching = ref(true);

const emit = defineEmits([ 'evocatCreated' ]);

onMounted(async () => {
    const schemaCategoryResult = await API.schemas.getCategory({ id: categoryId });
    const schemaUpdatesResult = await API.schemas.getCategoryUpdates({ id: categoryId });
    const datasourcesResult = await API.datasources.getAllDatasources({}, { categoryId });
    const mappingsResult = await API.mappings.getAllMappingsInCategory({}, { categoryId });
    fetching.value = false;

    if (!schemaCategoryResult.status || !schemaUpdatesResult.status || !datasourcesResult.status || !mappingsResult.status) {
        // TODO handle error
        return;
    }

    const schemaUpdates = schemaUpdatesResult.data.map(SchemaUpdate.fromServer);
    const logicalModels = logicalModelsFromServer(datasourcesResult.data, mappingsResult.data);

    const schemaCategory = Category.fromServer(schemaCategoryResult.data, logicalModels);
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
    const result = await API.schemas.updateCategory({ id: categoryId }, update);
    fetching.value = false;

    if (!result.status) {
        // TODO handle error
        console.log('Update failed');
        return result;
    }

    const schemaCategory = Category.fromServer(result.data, models);
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
