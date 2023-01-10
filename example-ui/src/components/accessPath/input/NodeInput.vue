<script setup lang="ts">
import { SelectionType, Graph, Node } from '@/types/categoryGraph';
import { onMounted, onUnmounted, ref, watch } from 'vue';

type NodeInputProps = {
    graph: Graph;
    modelValue?: Node;
};

const props = defineProps<NodeInputProps>();

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref<Node>();

watch(() => props.modelValue, (newValue?: Node) => {
    if (!newValue) {
        innerValue.value?.unselect();
        innerValue.value = undefined;
        return;
    }

    if (newValue.equals(innerValue.value))
        return;

    innerValue.value?.unselect();
    innerValue.value = newValue;
    innerValue.value.select({ type: SelectionType.Root, level: 0 });
});

onMounted(() => {
    props.graph.addNodeListener('tap', onNodeTapHandler);
});

onUnmounted(() => {
    props.graph.removeNodeListener('tap', onNodeTapHandler);
});

function onNodeTapHandler(node: Node) {
    innerValue.value?.unselect();

    if (node.equals(innerValue.value)) {
        // If we double tap current node, it become unselected.
        innerValue.value = undefined;
    }
    else {
        node.select({ type: SelectionType.Root, level: 0 });
        innerValue.value = node;
    }

    emit('update:modelValue', innerValue.value);
}
</script>

<template>
    <span>
        {{ innerValue?.schemaObject.label }}
    </span>
</template>
