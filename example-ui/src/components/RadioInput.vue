<script setup lang="ts">
import { ref, watch } from 'vue';

type RadioInputProps<T> = {
    modelValue: T;
    value: T;
    disabled?: boolean;
};

const props = withDefaults(defineProps<RadioInputProps<unknown>>(), {
    disabled: false,
});

const emit = defineEmits([ 'update:modelValue' ]);

const active = ref(props.value === props.modelValue);

watch(() => props.modelValue, (newValue: unknown) => {
    active.value = props.value === newValue;
});

function onClick() {
    emit('update:modelValue', props.value);
}
</script>

<template>
    <button
        :class="{ selected: active }"
        :disabled="disabled"
        @click="onClick"
    >
        <slot />
    </button>
</template>
