<script setup lang="ts">
import { ref, shallowRef } from 'vue';
import { Edge, SelectionType, type Node, Graph } from '@/types/categoryGraph';
import InstanceObjectDisplay from './InstanceObjectDisplay.vue';
import type { SchemaMorphism, SchemaObject } from '@/types/schema';
import InstanceMorphismDisplay from './InstanceMorphismDisplay.vue';
import type { Evocat } from '@/types/evocat/Evocat';
import EvocatDisplay from './EvocatDisplay.vue';

const evocat = shallowRef<Evocat>();

const selectedNode = ref<Node>();
const selectedEdge = ref<Edge>();

function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    const listener = context.graph.listen();
    listener.onNode('tap', node => selectNode(node));
    listener.onEdge('tap', edge => selectEdge(edge));
    listener.onCanvas('tap', onCanvasTapHandler);
}

function onCanvasTapHandler() {
    selectedNode.value?.unselect();
    selectedNode.value = undefined;
    selectedEdge.value?.unselect();
    selectedEdge.value = undefined;
}

function objectClicked(object: SchemaObject) {
    const newNode = evocat.value?.graph?.getNode(object);
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
    const newEdge = evocat.value?.graph?.getEdge(morphism);
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
        <EvocatDisplay @evocat-created="evocatCreated" />
        <InstanceObjectDisplay
            v-if="selectedNode"
            :key="selectedNode.schemaObject.key.value"
            :node="selectedNode"
            @object:click="objectClicked"
        />
        <InstanceMorphismDisplay
            v-if="selectedEdge"
            :key="selectedEdge.schemaMorphism.signature.value"
            :edge="selectedEdge"
            @object:click="objectClicked"
        />
    </div>
</template>
