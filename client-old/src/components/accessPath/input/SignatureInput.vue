<script setup lang="ts">
import type { Edge, Node } from '@/types/categoryGraph';
import type { SequenceSignature } from '@/types/accessPath/graph';
import { onMounted, onUnmounted, shallowRef, watch } from 'vue';
import type { Filter } from '@/types/categoryGraph/PathMarker';
import { useEvocat } from '@/utils/injects';

const { graph } = $(useEvocat());

type SignatureInputProps = {
    filter: Filter;
    modelValue: SequenceSignature;
    disabled?: boolean;
};

const props = withDefaults(defineProps<SignatureInputProps>(), {
    disabled: false,
});

const emit = defineEmits([ 'update:modelValue', 'input' ]);
const innerValue = shallowRef(props.modelValue.copy());

watch(() => props.modelValue, (newValue: SequenceSignature) => {
    if (!innerValue.value.equals(newValue))
        setSignature(newValue);
});

const listener = graph.listen();

onMounted(() => {
    listener.onNode('tap', onNodeTapHandler);
    listener.onEdge('tap', onEdgeTapHandler);
    innerValue.value.sequence.selectAll();
    innerValue.value.markAvailablePaths(props.filter);
});

onUnmounted(() => {
    listener.close();
    innerValue.value.sequence.unselectAll();
    graph.resetAvailabilityStatus();
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
    graph.resetAvailabilityStatus();
    innerValue.value.markAvailablePaths(props.filter);

    emit('update:modelValue', innerValue.value);
    emit('input');
}

function setSignature(signature: SequenceSignature) {
    innerValue.value.sequence.unselectAll();
    graph.resetAvailabilityStatus();
    innerValue.value = signature;
    innerValue.value.sequence.selectAll();
    innerValue.value.markAvailablePaths(props.filter);
}
</script>

<template>
    <div v-if="false" />
</template>

