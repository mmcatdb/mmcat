<script setup lang="ts">
import type { Edge, Node } from '@/types/categoryGraph';
import type { SequenceSignature } from '@/types/accessPath/graph';
import { onMounted, onUnmounted, ref, watch } from 'vue';
import type { Filter } from '@/types/categoryGraph/PathMarker';
import { useEvocat } from '@/utils/injects';

const evocat = $(useEvocat());

type SignatureInputProps = {
    filter: Filter;
    modelValue: SequenceSignature;
    disabled?: boolean;
};

const props = withDefaults(defineProps<SignatureInputProps>(), {
    disabled: false,
});

const emit = defineEmits([ 'update:modelValue', 'input' ]);
const innerValue = ref(props.modelValue.copy());

watch(() => props.modelValue, (newValue: SequenceSignature) => {
    if (!innerValue.value.equals(newValue))
        setSignature(newValue);
});

onMounted(() => {
    evocat.graph.addNodeListener('tap', onNodeTapHandler);
    evocat.graph.addEdgeListener('tap', onEdgeTapHandler);
    innerValue.value.sequence.selectAll();
    innerValue.value.markAvailablePaths(props.filter);
});

onUnmounted(() => {
    evocat.graph.removeNodeListener('tap', onNodeTapHandler);
    evocat.graph.removeEdgeListener('tap', onEdgeTapHandler);
    innerValue.value.sequence.unselectAll();
    evocat.graph.resetAvailabilityStatus();
});

function onNodeTapHandler(node: Node): void {
    if (props.disabled)
        return;

    if (innerValue.value.sequence.tryRemoveNode(node) || innerValue.value.sequence.tryAddNode(node))
        innerValueUpdated();
}

function onEdgeTapHandler(edge: Edge): void {
    if (props.disabled)
        return;

    if (innerValue.value.sequence.tryAddEdge(edge))
        innerValueUpdated();
}

function innerValueUpdated() {
    evocat.graph.resetAvailabilityStatus();
    innerValue.value.markAvailablePaths(props.filter);

    emit('update:modelValue', innerValue.value);
    emit('input');
}

function setSignature(signature: SequenceSignature) {
    innerValue.value.sequence.unselectAll();
    evocat.graph.resetAvailabilityStatus();
    innerValue.value = signature;
    innerValue.value.sequence.selectAll();
    innerValue.value.markAvailablePaths(props.filter);
}
</script>

<template>
    <div v-if="false" />
</template>

