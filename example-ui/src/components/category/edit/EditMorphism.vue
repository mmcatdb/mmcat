<script setup lang="ts">
import { Edge, SelectionType, type Graph, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import MinimumInput from './MinimumInput.vue';

import IriDisplay from '@/components/IriDisplay.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

enum NodeIndices {
    First = 0,
    Second = 1
}

type EditMorphismProps = {
    graph: Graph;
    edge: Edge;
};

const props = defineProps<EditMorphismProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const node1 = ref<Node | null>(null);
const node2 = ref<Node | null>(null);
const label = ref(props.edge.schemaMorphism.label);
const lastSelectedNode = ref(NodeIndices.First);
const temporayEdge = ref<TemporaryEdge | null>(null);
const min = ref(props.edge.schemaMorphism.min);

const nodesSelected = computed(() => !!node1.value && !!node2.value);
const changed = computed(() =>
    !props.edge.domainNode.equals(node1.value)
            || !props.edge.codomainNode.equals(node2.value)
            || props.edge.schemaMorphism.label !== label.value.trim()
            || props.edge.schemaMorphism.min !== min.value,
);
const nodesChanged = computed(() => !props.edge.domainNode.equals(node1.value) || !props.edge.codomainNode.equals(node2.value));
const isNew = computed(() => props.edge.schemaMorphism.isNew);

defineExpose({ changed });

onMounted(() => {
    if (isNew.value)
        props.graph.addNodeListener('tap', onNodeTapHandler);

    onNodeTapHandler(props.edge.domainNode);
    onNodeTapHandler(props.edge.codomainNode);
});

onUnmounted(() => {
    props.graph.removeNodeListener('tap', onNodeTapHandler);
    unselectAll();
});

function save() {
    if (!node1.value || !node2.value)
        return;

    // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (if only the cardinality changed).

    props.graph.schemaCategory.editMorphism(props.edge.schemaMorphism, node1.value.schemaObject, node2.value.schemaObject, min.value, label.value.trim());

    temporayEdge.value?.delete();
    props.graph.deleteEdge(props.edge);
    props.graph.createEdge(props.edge.schemaMorphism, 'new');
    props.graph.layout();

    emit('save');
}

function cancel() {
    emit('cancel');
}

function deleteFunction() {
    // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (if only the cardinality changed).

    props.graph.schemaCategory.deleteMorphism(props.edge.schemaMorphism);
    props.graph.deleteEdge(props.edge);

    emit('save');
}

function unselectAll() {
    node1.value?.unselect();
    node2.value?.unselect();
    temporayEdge.value?.delete();
}

function selectAll() {
    node1.value?.select({ type: SelectionType.Selected, level: 0 });
    node2.value?.select({ type: SelectionType.Selected, level: 1 });
    if (nodesChanged.value)
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
        <h2>Edit Schema Morphism</h2>
        <ValueContainer>
            <ValueRow label="Domain obje">
                {{ node1?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Codomain ob">
                {{ node2?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Label?:">
                <input v-model="label" />
            </ValueRow>
            <ValueRow label="Iri:">
                <IriDisplay
                    :iri="edge.schemaMorphism.iri"
                    :max-chars="36"
                />
            </ValueRow>
            <ValueRow label="Pim Iri:">
                <IriDisplay
                    :iri="edge.schemaMorphism.pimIri"
                    :max-chars="36"
                />
            </ValueRow>
            <ValueRow label="Signature:">
                {{ edge.schemaMorphism.signature }}
            </ValueRow>
            <MinimumInput
                v-model="min"
                :disabled="!isNew"
            />
        </ValueContainer>
        <div class="button-row">
            <button
                v-if="isNew"
                :disabled="!nodesSelected || !changed"
                @click="save"
            >
                Confirm
            </button>
            <button
                v-if="isNew"
                :disabled="!nodesSelected"
                @click="switchNodes"
            >
                Switch
            </button>
            <button @click="cancel">
                Cancel
            </button>
            <button
                @click="deleteFunction"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style scoped>
.number-input {
    max-width: 80px;
}
</style>

