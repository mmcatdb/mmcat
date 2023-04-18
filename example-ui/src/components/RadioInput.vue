<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        modelValue: {
            type: undefined,
            required: true,
        },
        value: {
            type: undefined,
            required: true,
        },
        disabled: {
            type: Boolean,
            required: false,
            default: false,
        },
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            active: this.value === this.modelValue,
        };
    },
    watch: {
        modelValue: {
            handler(newValue: string): void {
                this.active = this.value === newValue;
            },
        },
    },
    methods: {
        onClick() {
            this.$emit('update:modelValue', this.value);
        },
    },
});
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
