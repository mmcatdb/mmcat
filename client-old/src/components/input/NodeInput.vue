<script setup lang="ts">
import type { SelectionType, Node } from '@/types/categoryGraph';
import { onMounted, onUnmounted, shallowRef, watch } from 'vue';
import { useEvocat } from '@/utils/injects';

const { graph } = $(useEvocat());

type NodeInputProps = {
    modelValue: (Node | undefined)[];
    count?: number;
    type: SelectionType;
    disabled?: boolean;
};

const props = defineProps<NodeInputProps>();

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = shallowRef([ ...props.modelValue ]);
// The index can't be greater than count - 1 (if count is defined).
const lastIndex = shallowRef((props.count && props.modelValue.length >= props.count) ? props.count - 1 : props.modelValue.length);

watch(() => props.modelValue, (newValue: (Node | undefined)[]) => {
    innerValue.value.forEach(node => node?.unselect());
    innerValue.value = [ ...newValue ];
    innerValue.value.forEach((node, index) => node?.select({ type: props.type, level: index }));
}, { immediate: true });

const listener = graph.listen();

onMounted(() => {
    innerValue.value.forEach((node, index) => node?.select({ type: props.type, level: index }));
    if (!props.disabled)
        listener.onNode('tap', onNodeTapHandler);
});

onUnmounted(() => {
    innerValue.value.forEach(node => node?.unselect());
    listener.close();
});

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
