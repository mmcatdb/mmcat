<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue';
import API from '@/utils/api';
import { SchemaCategory, type PositionUpdate } from '@/types/schema';
import cytoscape from 'cytoscape';
import fcose from 'cytoscape-fcose';
import layoutUtilities from 'cytoscape-layout-utilities';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { Graph } from '@/types/categoryGraph';
import { style } from './defaultGraphStyle';
import { useSchemaCategoryId } from '@/utils/globalSchemaSettings';
import { LogicalModel } from '@/types/logicalModel';

cytoscape.use(fcose);
cytoscape.use(layoutUtilities);

const emit = defineEmits([ 'create:graph' ]);

const logicalModels = ref<LogicalModel[]>([]);
const schemaFetched = ref(false);
const saveButtonDisabled = ref(false);
const graph = ref<Graph>();

const categoryId = useSchemaCategoryId();

onMounted(async () => {
    const result = await API.schemas.getCategoryWrapper({ id: categoryId });
    // TODO
    const logicalModelsResult = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    if (!result.status || !logicalModelsResult.status)
        return;

    console.log(result.data);
    const schemaCategory = SchemaCategory.fromServer(result.data);
    logicalModels.value = logicalModelsResult.data.map(LogicalModel.fromServer);

    graph.value = createGraph(schemaCategory, logicalModels.value);

    schemaFetched.value = true;
    emit('create:graph', graph.value);
});

function createGraph(schema: SchemaCategory, logicalModels: LogicalModel[]): Graph {
    const container = document.getElementById('cytoscape');

    // This is needed because of some weird bug.
    // It has to do something with the cache (because it doesn't appear after hard refresh).
    // It causes the cytoscape div to contain two cytoscape canvases (the first one is empty, probably it's here from the previous instance).
    // Weird is this only occurs after 'build', not 'dev' (meaning 'serve').
    if (container) {
        let child = container.lastElementChild;
        while (child) {
            container.removeChild(child);
            child = container.lastElementChild;
        }
    }

    const cytoscapeInstance = cytoscape({
        container,
        //layout: { name: 'preset' },
        //elements,
        style,
        boxSelectionEnabled: true,
        wheelSensitivity: 0.3,
        maxZoom: 2
    });

    logicalModels.forEach(logicalModel => {
        logicalModel.mappings.forEach(mapping => {
            schema.setDatabaseToObjectsFromMapping(mapping, logicalModel);
        });
    });

    const newGraph = new Graph(cytoscapeInstance, schema);
    schema.objects.forEach(object => newGraph.createNode(object));

    // First we create a dublets of morphisms. Then we create edges from them.
    const sortedBaseMorphisms = schema.morphisms.filter(morphism => morphism.isBase)
        .sort((m1, m2) => m1.sortBaseValue - m2.sortBaseValue);
    const morphismDublets = [];
    //for (let i = 0; i < sortedBaseMorphisms.length; i += 2)
    for (let i = 0; i < sortedBaseMorphisms.length; i += 2)
        morphismDublets.push({ morphism: sortedBaseMorphisms[i], dualMorphism: sortedBaseMorphisms[i + 1] });

    morphismDublets.forEach(dublet => newGraph.createEdgeWithDual(dublet.morphism));

    // Position the object to the center of the canvas.
    newGraph.fixLayout();
    newGraph.layout();
    newGraph.center();

    return newGraph;
}

async function savePositionChanges() {
    /*
    if (!graph.value)
        return;

    saveButtonDisabled.value = true;
    console.log('Saving position changes');

    const updatedPositions = graph.value.schemaCategory.objects
        .map(object => object.toPositionUpdate())
        .filter((update): update is PositionUpdate => update !== null);

    const result = await API.schemas.updateCategoryPositions({ id: graph.value.schemaCategory.id }, updatedPositions);
    console.log(result);

    saveButtonDisabled.value = false;
    */
}

function updateSchema(schemaCategory: SchemaCategory) {
    schemaFetched.value = false;
    graph.value = undefined;

    nextTick(() => {
        graph.value = createGraph(schemaCategory, logicalModels.value);
        schemaFetched.value = true;
        emit('create:graph', graph.value);
    });
}

defineExpose({
    updateSchema
});
</script>

<template>
    <div class="graph-display">
        <div
            id="cytoscape"
        />
        <template v-if="graph">
            <div class="category-command-panel button-panel">
                <button
                    :disabled="saveButtonDisabled"
                    @click="savePositionChanges"
                >
                    Save positions
                </button>
                <button
                    @click="graph?.center()"
                >
                    Center graph
                </button>
                <button
                    @click="graph?.resetLayout()"
                >
                    Reset layout
                </button>
            </div>
        </template>
        <ResourceNotFound v-else-if="schemaFetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
#cytoscape {
    width: var(--schema-category-canvas-width);
    height: var(--schema-category-canvas-height);
    background-color: var(--color-background-canvas);
}

.graph-display {
    display: flex;
    flex-direction: column;
    margin-right: 16px;
}

.category-command-panel {
    padding: 8px 8px;
    background-color: var(--color-background-dark);
}
</style>
