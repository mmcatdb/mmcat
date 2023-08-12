<script setup lang="ts">
import { onMounted, shallowRef } from 'vue';
import cytoscape from 'cytoscape';
import fcose from 'cytoscape-fcose';
import layoutUtilities from 'cytoscape-layout-utilities';
import { Graph } from '@/types/categoryGraph';
import { style } from './defaultGraphStyle';

cytoscape.use(fcose);
cytoscape.use(layoutUtilities);

const emit = defineEmits([ 'graphCreated', 'updatePositions' ]);

const graph = shallowRef<Graph>();

withDefaults(defineProps<{ fetching?: boolean }>(), {
    fetching: false,
});

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
</script>

<template>
    <div class="graph-display">
        <div
            id="cytoscape"
        />
        <template v-if="graph">
            <div class="category-command-panel button-panel">
                <button
                    :disabled="fetching"
                    @click="() => emit('updatePositions')"
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
