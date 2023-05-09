<script setup lang="ts">
import type { SelectionType, Node } from '@/types/categoryGraph';
import { ref, watch } from 'vue';
import NodeInput from './NodeInput.vue';

type SingleNodeInputProps = {
    modelValue?: Node;
    type: SelectionType;
    disabled?: boolean;
};

const props = defineProps<SingleNodeInputProps>();

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref(props.modelValue);

watch(() => props.modelValue, (newValue?: Node) => {
    innerValue.value = newValue;
});

function onValueUpdate(newValue: Node[]) {
    if (newValue[0] === innerValue.value)
        return;

    if (newValue[0]?.equals(innerValue.value))
        return;

    innerValue.value = newValue[0];
    emit('update:modelValue', innerValue.value);
}
</script>

<template>
    <NodeInput
        :model-value="[ innerValue ]"
        :count="1"
        :type="type"
        :disabled="disabled"
        @update:model-value="onValueUpdate"
    />
    <span>
        {{ innerValue?.schemaObject.label }}
    </span>
</template>
