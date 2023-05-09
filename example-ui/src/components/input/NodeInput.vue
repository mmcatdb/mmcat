<script setup lang="ts">
import type { SelectionType, Node } from '@/types/categoryGraph';
import { useEvocat } from '@/utils/injects';
import { onMounted, onUnmounted, ref, watch } from 'vue';

const evocat = $(useEvocat());

type NodeInputProps = {
    modelValue: (Node | undefined)[];
    count?: number;
    type: SelectionType;
    disabled?: boolean;
};

const props = defineProps<NodeInputProps>();

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref([ ...props.modelValue ]);
// The index can't be greater than count - 1 (if count is defined).
const lastIndex = ref((props.count && props.modelValue.length >= props.count) ? props.count - 1 : props.modelValue.length);

watch(() => props.modelValue, (newValue: (Node | undefined)[]) => {
    innerValue.value.forEach(node => node?.unselect());
    innerValue.value = [ ...newValue ];
    innerValue.value.forEach((node, index) => node?.select({ type: props.type, level: index }));
}, { immediate: true });

onMounted(() => {
    if (!props.disabled)
        evocat.graph.addNodeListener('tap', onNodeTapHandler);
});

onUnmounted(() => {
    evocat.graph.removeNodeListener('tap', onNodeTapHandler);
    innerValue.value.forEach(node => node?.unselect());
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
    const nodeIndex = nodes.findIndex(n => n && n.equals(node));

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
