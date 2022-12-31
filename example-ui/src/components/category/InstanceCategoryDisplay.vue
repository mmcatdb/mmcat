<script setup lang="ts">
import { ref } from 'vue';
import { SelectionType, type Node } from '@/types/categoryGraph';
import type { Graph } from '@/types/categoryGraph';
import InstanceObjectDisplay from './InstanceObjectDisplay.vue';
import GraphDisplay from './GraphDisplay.vue';
import type { SchemaObject } from '@/types/schema';

const graph = ref<Graph>();
const selectedNode = ref<Node>();

function cytoscapeCreated(newGraph: Graph) {
    graph.value = newGraph;
    graph.value.addNodeListener('tap', node => selectNode(node));
}

function objectClicked(object: SchemaObject) {
    const newNode = graph.value?.getNode(object);
    if (newNode)
        selectNode(newNode);
}

function selectNode(node: Node) {
    selectedNode.value?.unselect();

    if (node.equals(selectedNode.value)) {
        selectedNode.value = undefined;
        return;
    }

    selectedNode.value = node;
    selectedNode.value.select({ type: SelectionType.Root, level: 0 });
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
    </div>
</template>

<style scoped>

</style>
