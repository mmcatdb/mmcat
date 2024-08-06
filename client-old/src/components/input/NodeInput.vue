<script setup lang="ts">
import type { SelectionType, Node, Graph } from '@/types/categoryGraph';
import { onMounted, onUnmounted, shallowRef, watch, computed, watchEffect } from 'vue';
import { useEvocat } from '@/utils/injects';

//const { graph } = $(useEvocat());

type NodeInputProps = {
    modelValue: (Node | undefined)[];
    graph?: any; // optional graph prop
    count?: number;
    type: SelectionType;
    disabled?: boolean;
};

const props = defineProps<NodeInputProps>();

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = shallowRef([ ...props.modelValue ]);
// The index can't be greater than count - 1 (if count is defined).
const lastIndex = shallowRef((props.count && props.modelValue.length >= props.count) ? props.count - 1 : props.modelValue.length);

const graph = computed(() => {
    if (props.graph) {
        return props.graph;
    }
    const evocat = useEvocat();
    return evocat.graph;
});

watch(() => props.modelValue, (newValue: (Node | undefined)[]) => {
    innerValue.value.forEach(node => node?.unselect());
    innerValue.value = [ ...newValue ];
    innerValue.value.forEach((node, index) => node?.select({ type: props.type, level: index }));
}, { immediate: true });

let listener: any;

onMounted(() => {
    if (!graph.value) return;

    listener = graph.value.listen();

    innerValue.value.forEach((node, index) => node?.select({ type: props.type, level: index }));
    if (!props.disabled) {
        listener.onNode('tap', onNodeTapHandler);
    }
});

onUnmounted(() => {
    if (listener) {
        innerValue.value.forEach(node => node?.unselect());
        listener.close();
    }
});
/*
const listener = graph.listen();

onMounted(() => {
    innerValue.value.forEach((node, index) => node?.select({ type: props.type, level: index }));
    if (!props.disabled)
        listener.onNode('tap', onNodeTapHandler);
});

onUnmounted(() => {
    innerValue.value.forEach(node => node?.unselect());
    listener.close();
});*/

function getNextAvailableIndex(): number {
    for (let i = 0; !props.count || i < props.count; i++) {
        if (!innerValue.value[i])
            return i;
    }

    return lastIndex.value;
}

function onNodeTapHandler(node: Node) {
    const nodes = innerValue.value;
    const nodeIndex = nodes.findIndex(n => n?.equals(node));

    if (nodeIndex !== -1) {
        node.unselect();
        nodes[nodeIndex] = undefined;
        lastIndex.value = nodeIndex;
    }
    else {
        const nextIndex = getNextAvailableIndex();
        lastIndex.value = nextIndex;

        nodes[nextIndex]?.unselect();
        nodes[nextIndex] = node;
        node.select({ type: props.type, level: nextIndex });
    }

    emit('update:modelValue', innerValue.value);
}
</script>

<template />
