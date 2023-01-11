<script setup lang="ts">
import { ref } from 'vue';
import { Edge, SelectionType, type TemporaryEdge, type Node } from '@/types/categoryGraph';
import type { Graph } from '@/types/categoryGraph';
import InstanceObjectDisplay from './InstanceObjectDisplay.vue';
import GraphDisplay from './GraphDisplay.vue';
import type { SchemaMorphism, SchemaObject } from '@/types/schema';
import InstanceMorphismDisplay from './InstanceMorphismDisplay.vue';

const graph = ref<Graph>();
const selectedNode = ref<Node>();
const selectedEdge = ref<Edge>();

function cytoscapeCreated(newGraph: Graph) {
    graph.value = newGraph;
    graph.value.addNodeListener('tap', node => selectNode(node));
    graph.value.addEdgeListener('tap', edge => selectEdge(edge));
    graph.value.addCanvasListener('tap', onCanvasTapHandler);
}

function onCanvasTapHandler() {
    selectedNode.value?.unselect();
    selectedNode.value = undefined;
    selectedEdge.value?.unselect();
    selectedEdge.value = undefined;
}

function objectClicked(object: SchemaObject) {
    const newNode = graph.value?.getNode(object);
    if (newNode)
        selectNode(newNode);
}

function selectNode(node: Node) {
    selectedNode.value?.unselect();
    selectedEdge.value?.unselect();
    selectedEdge.value = undefined;

    if (node.equals(selectedNode.value)) {
        selectedNode.value = undefined;
        return;
    }

    selectedNode.value = node;
    selectedNode.value.select({ type: SelectionType.Root, level: 0 });
}

function edgeClicked(morphism: SchemaMorphism) {
    const newEdge = graph.value?.getEdge(morphism);
    if (newEdge)
        selectEdge(newEdge);
}

function selectEdgeInner(edge: Edge) {
    edge.domainNode.select({ type: SelectionType.Selected, level: 0 });
    edge.codomainNode.select({ type: SelectionType.Selected, level: 1 });
}

function selectEdge(edge: Edge) {
    selectedEdge.value?.unselect();
    selectedNode.value?.unselect();
    selectedNode.value = undefined;

    if (edge.equals(selectedEdge.value)) {
        selectedEdge.value = undefined;
        return;
    }

    selectedEdge.value = edge;
    selectEdgeInner(edge);
}
</script>

<template>
    <div class="divide">
        <GraphDisplay @create:graph="cytoscapeCreated" />
        <InstanceObjectDisplay
            v-if="selectedNode"
            :key="selectedNode.schemaObject.key.value"
            :node="selectedNode"
            @object:click="objectClicked"
        />
        <InstanceMorphismDisplay
            v-if="selectedEdge"
            :key="selectedEdge.schemaMorphism.signature.toString()"
            :edge="selectedEdge"
            @object:click="objectClicked"
        />
    </div>
</template>

<style scoped>

</style>
