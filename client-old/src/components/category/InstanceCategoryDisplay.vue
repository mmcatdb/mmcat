<script setup lang="ts">
import { shallowRef } from 'vue';
import { Edge, SelectionType, type Node, Graph } from '@/types/categoryGraph';
import InstanceObjexDisplay from './InstanceObjexDisplay.vue';
import type { Category, SchemaObjex } from '@/types/schema';
import InstanceMorphismDisplay from './InstanceMorphismDisplay.vue';
import type { Evocat } from '@/types/evocat/Evocat';
import EvocatDisplay from './EvocatDisplay.vue';
import { InstanceCategory, type InstanceMorphism, type InstanceObjex } from '@/types/instance';
import API from '@/utils/api';

const evocat = shallowRef<Evocat>();
const instance = shallowRef<InstanceCategory>();
const error = shallowRef<unknown>();

async function fetchInstance(schema: Category) {
    const result = await API.instances.getInstanceCategory({});
    if (!result.status) {
        error.value = result.error;
        return;
    }

    instance.value = InstanceCategory.fromServer(result.data, schema);
}

function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    const listener = context.graph.listen();
    listener.onNode('tap', node => selectNode(node));
    listener.onEdge('tap', edge => selectEdge(edge));
    listener.onCanvas('tap', unselect);

    fetchInstance(context.evocat.schemaCategory);
}

type Selected = {
    type: 'node';
    node: Node;
    objex: InstanceObjex;
} | {
    type: 'edge';
    edge: Edge;
    morphism: InstanceMorphism;
};

const selected = shallowRef<Selected>();

function unselect() {
    if (selected.value?.type === 'node')
        selected.value.node.unselect();
    else if (selected.value?.type === 'edge')
        selected.value.edge.unselect();

    selected.value = undefined;
}

function objexClicked(objex: SchemaObjex) {
    const newNode = evocat.value?.graph?.getNode(objex.key);
    if (newNode)
        selectNode(newNode);
}

function selectNode(node: Node) {
    const isSameNode = selected.value?.type === 'node' && selected.value.node.equals(node);
    unselect();

    if (isSameNode)
        return;

    const objex = instance.value?.objexes.get(node.schemaObjex.key);
    if (!objex)
        return;

    selected.value = { type: 'node', node, objex };
    node.select({ type: SelectionType.Root, level: 0 });
}

function selectEdge(edge: Edge) {
    const isSameEdge = selected.value?.type === 'edge' && selected.value.edge.equals(edge);
    unselect();

    if (isSameEdge)
        return;

    const morphism = instance.value?.morphisms.get(edge.schemaMorphism.signature);
    if (!morphism)
        return;

    selected.value = { type: 'edge', edge, morphism };
    edge.domainNode.select({ type: SelectionType.Selected, level: 0 });
    edge.codomainNode.select({ type: SelectionType.Selected, level: 1 });
}
</script>

<template>
    <div class="divide">
        <EvocatDisplay @evocat-created="evocatCreated" />
        <InstanceObjexDisplay
            v-if="selected?.type === 'node'"
            :key="selected.node.schemaObjex.key.value"
            :node="selected.node"
            :objex="selected.objex"
            @objex:click="objexClicked"
        />
        <InstanceMorphismDisplay
            v-if="selected?.type === 'edge'"
            :key="selected.edge.schemaMorphism.signature.value"
            :edge="selected.edge"
            :morphism="selected.morphism"
            @objex:click="objexClicked"
        />
        <div
            v-if="error"
            class="text-danger"
        >
            Instance doens't exist yet.
        </div>
    </div>
</template>
