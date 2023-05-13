<script setup lang="ts">
import { onMounted, ref, shallowRef } from 'vue';
import cytoscape from 'cytoscape';
import fcose from 'cytoscape-fcose';
import layoutUtilities from 'cytoscape-layout-utilities';
import { Graph } from '@/types/categoryGraph';
import { style } from './defaultGraphStyle';

cytoscape.use(fcose);
cytoscape.use(layoutUtilities);

const emit = defineEmits([ 'graphCreated' ]);

const graph = shallowRef<Graph>();

const saveButtonDisabled = ref(false);

onMounted(() => {
    const newGraph = createGraph();
    graph.value = newGraph;
    emit('graphCreated', newGraph);
});

function getContainer(): HTMLElement | undefined {
    const container = document.getElementById('cytoscape') ?? undefined;

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

    return container;
}

function createGraph(): Graph {
    const container = getContainer();
    if (!container)
        throw new Error('Container for graph not found');

    const cytoscapeInstance = cytoscape({
        container,
        //layout: { name: 'preset' },
        //elements,
        style,
        boxSelectionEnabled: true,
        wheelSensitivity: 0.3,
        maxZoom: 2,
    });

    return new Graph(cytoscapeInstance);
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

/*
function updateSchema(schemaCategory: SchemaCategory) {
    schemaFetched.value = false;
    graph.value = undefined;

    nextTick(() => {
        graph.value = createGraph(schemaCategory, logicalModels.value);
        schemaFetched.value = true;
        emit('graphCreated', graph.value);
    });
}
*/

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
                    @click="graph?.center"
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
