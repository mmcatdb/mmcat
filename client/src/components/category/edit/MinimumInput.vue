<script setup lang="ts">
import { Cardinality, type Min } from '@/types/schema';
import RadioInput from '@/components/input/RadioInput.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { ref, watch } from 'vue';

type MinimumInputProps = {
    modelValue: Min;
    disabled?: boolean;
};

const props = defineProps<MinimumInputProps>();

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref(props.modelValue);

watch(() => props.modelValue, (newValue: Min) => {
    innerValue.value = newValue;
});

function updateInnerValue(newValue: Min): void {
    innerValue.value = newValue;
    emit('update:modelValue', innerValue.value);
}
</script>

<template>
    <ValueRow label="Minimum:">
        <RadioInput
            :model-value="innerValue"
            :value="Cardinality.Zero"
            :disabled="disabled"
            @update:model-value="updateInnerValue"
        >
            0
        </RadioInput>
        <RadioInput
            :model-value="innerValue"
            :value="Cardinality.One"
            :disabled="disabled"
            @update:model-value="updateInnerValue"
        >
            1
        </RadioInput>
    </ValueRow>
</template>

<style scoped>
.value button:first-of-type {
    margin-right: 4px;
}
</style>

