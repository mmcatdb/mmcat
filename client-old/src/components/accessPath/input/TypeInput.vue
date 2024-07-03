<script setup lang="ts">
import { PropertyType } from '@/types/categoryGraph';
import { ref, watch } from 'vue';

const props = defineProps<{ modelValue: PropertyType }>();

const emit = defineEmits([ 'update:modelValue' ]);

const innerValue = ref(props.modelValue);

watch(() => props.modelValue, (newValue: PropertyType) => {
    if (newValue !== innerValue.value)
        innerValue.value = newValue;
});

function updateInnerValue() {
    emit('update:modelValue', innerValue.value);
}
</script>

<template>
    <input
        id="simple"
        v-model="innerValue"
        type="radio"
        :value="PropertyType.Simple"
        @change="updateInnerValue"
    />
    <label
        :class="{ value: innerValue === PropertyType.Simple }"
        for="simple"
    >
        Simple
    </label><br />
    <input
        id="complex"
        v-model="innerValue"
        type="radio"
        :value="PropertyType.Complex"
        @change="updateInnerValue"
    />
    <label
        :class="{ value: innerValue === PropertyType.Complex }"
        for="complex"
    >
        Complex
    </label>
</template>
