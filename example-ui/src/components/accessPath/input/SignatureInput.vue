<script setup lang="ts">
import type { Edge, Graph, Node, FilterFunction } from '@/types/categoryGraph';
import { SequenceSignature } from '@/types/accessPath/graph';
import { onMounted, onUnmounted, ref, watch } from 'vue';

type SignatureInputProps = {
    graph: Graph;
    filter: { function: FilterFunction | FilterFunction[] };
    modelValue: SequenceSignature;
    defaultIsNull?: boolean;
    disabled?: boolean;
};

const props = withDefaults(defineProps<SignatureInputProps>(), {
    defaultIsNull: false,
    disabled: false
});

const emit = defineEmits([ 'update:modelValue', 'input' ]);
const innerValue = ref(props.modelValue.copy());

watch(() => props.modelValue, (newValue: SequenceSignature) => {
    if (!innerValue.value.equals(newValue))
        setSignature(newValue);
});

onMounted(() => {
    props.graph.addNodeListener('tap', onNodeTapHandler);
    props.graph.addEdgeListener('tap', onEdgeTapHandler);
    innerValue.value.sequence.selectAll();
    innerValue.value.markAvailablePaths(props.filter.function);
});

onUnmounted(() => {
    props.graph.removeNodeListener('tap', onNodeTapHandler);
    props.graph.removeEdgeListener('tap', onEdgeTapHandler);
    innerValue.value.sequence.unselectAll();
    props.graph.resetAvailabilityStatus();
});

function onNodeTapHandler(node: Node): void {
    if (props.disabled)
        return;

    if (innerValue.value.sequence.tryRemoveNode(node) || innerValue.value.sequence.tryAddNode(node))
        updateInnerValue();
}

function onEdgeTapHandler(edge: Edge): void {
    if (props.disabled)
        return;

    if (innerValue.value.sequence.tryAddEdge(edge) || innerValue.value.sequence.tryAddEdge(edge.dual))
        updateInnerValue();
}

function updateInnerValue() {
    if (!innerValue.value.isEmpty && innerValue.value.isNull)
        innerValue.value = innerValue.value.copyNotNull();

    if (innerValue.value.isEmpty && props.defaultIsNull)
        innerValue.value = SequenceSignature.null(innerValue.value.sequence.rootNode);

    props.graph.resetAvailabilityStatus();
    innerValue.value.markAvailablePaths(props.filter.function);
    sendUpdate();
}

function setSignature(signature: SequenceSignature) {
    innerValue.value.sequence.unselectAll();
    props.graph.resetAvailabilityStatus();
    innerValue.value = signature;
    innerValue.value.sequence.selectAll();
    innerValue.value.markAvailablePaths(props.filter.function);
}

function sendUpdate() {
    emit('update:modelValue', innerValue.value);
    emit('input');
}
</script>

<template>
    <div v-if="false" />
</template>

