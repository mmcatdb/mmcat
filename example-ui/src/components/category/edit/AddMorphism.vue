<script setup lang="ts">
import { SelectionType, type Graph, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality, type Min } from '@/types/schema';
import { onMounted, onUnmounted, ref } from 'vue';
import MinimumInput from './MinimumInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { computed } from '@vue/reactivity';

enum NodeIndices {
    First = 0,
    Second = 1
}

type AddMorphismProps = {
    graph: Graph;
};

const props = defineProps<AddMorphismProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const node1 = ref<Node | null>(null);
const node2 = ref<Node | null>(null);
const label = ref('');
const iri = ref('');
const pimIri = ref('');
const lastSelectedNode = ref(NodeIndices.First);
const temporayEdge = ref<TemporaryEdge | null>(null);
const min = ref<Min>(Cardinality.One);

const nodesSelected = computed(() => !!node1.value && !!node2.value);
const iriIsAvailable = computed(() => props.graph.schemaCategory.iriIsAvailable(iri.value));

onMounted(() => props.graph.addNodeListener('tap', onNodeTapHandler));

onUnmounted(() => {
    props.graph.removeNodeListener('tap', onNodeTapHandler);
    unselectAll();
});

function save() {
    if (!node1.value || !node2.value)
        return;

    if (iri.value) {
        const morphism = props.graph.schemaCategory.createMorphismWithIri(node1.value.schemaObject, node2.value.schemaObject, min.value, iri.value, pimIri.value, label.value);
        if (!morphism)
            return;

        temporayEdge.value?.delete();
        props.graph.createEdge(morphism, 'new');
    }
    else {
        const morphism = props.graph.schemaCategory.createMorphism(node1.value.schemaObject, node2.value.schemaObject, min.value, label.value);
        temporayEdge.value?.delete();
        props.graph.createEdge(morphism, 'new');
    }

    emit('save');
}

function cancel() {
    emit('cancel');
}

function unselectAll() {
    node1.value?.unselect();
    node2.value?.unselect();
    temporayEdge.value?.delete();
}

function selectAll() {
    node1.value?.select({ type: SelectionType.Selected, level: 0 });
    node2.value?.select({ type: SelectionType.Selected, level: 1 });
    temporayEdge.value = (!!node1.value && !!node2.value) ? props.graph.createTemporaryEdge(node1.value, node2.value) : null;
}

function onNodeTapHandler(node: Node): void {
    unselectAll();

    let changed = false;
    if (node.equals(node1.value)) {
        node1.value = null;
        changed = true;
    }

    if (node.equals(node2.value)) {
        node2.value = null;
        changed = true;
    }

    if (!changed)
        handleTapOnNotSelectedNode(node);

    selectAll();
}

function handleTapOnNotSelectedNode(node: Node) {
    // Which node should be changed.
    const changingNodeIndex = node1.value === null
        ? NodeIndices.First
        : node2.value === null
            ? NodeIndices.Second
            : lastSelectedNode.value;

    setNodeOnIndex(node, changingNodeIndex);
    lastSelectedNode.value = changingNodeIndex;
}

function setNodeOnIndex(node: Node, index: NodeIndices) {
    if (index === NodeIndices.First)
        node1.value = node;
    else
        node2.value = node;
}

function switchNodes() {
    if (node1.value === null || node2.value === null)
        return;

    const swap = node1.value;
    node1.value = node2.value;
    node2.value = swap;

    node1.value.select({ type: SelectionType.Selected, level: 0 });
    node2.value.select({ type: SelectionType.Selected, level: 1 });
}
</script>

<template>
    <div class="add-morphism">
        <h2>Add Schema Morphism</h2>
        <ValueContainer>
            <ValueRow label="Domain object:">
                {{ node1?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Codomain object:">
                {{ node2?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Label?:">
                <input v-model="label" />
            </ValueRow>
            <ValueRow label="Iri?:">
                <input v-model="iri" />
            </ValueRow>
            <ValueRow label="Pim Iri?:">
                <input v-model="pimIri" />
            </ValueRow>
            <MinimumInput
                v-model="min"
            />
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="!nodesSelected || !iriIsAvailable"
                @click="save"
            >
                Confirm
            </button>
            <button
                :disabled="!nodesSelected"
                @click="switchNodes"
            >
                Switch
            </button>
            <button @click="cancel">
                Cancel
            </button>
        </div>
    </div>
</template>

<style>
.number-input {
    max-width: 80px;
}
</style>

