<script setup lang="ts">
import IconChevronDown from '../icons/IconChevronDown.vue';
import IconChevronUp from '../icons/IconChevronUp.vue';

type OpenCloseToggleProps = {
    modelValue: boolean;
    disabled?: boolean;
};

const props = withDefaults(defineProps<OpenCloseToggleProps>(), {
    disabled: false,
});

const emit = defineEmits([ 'update:modelValue' ]);

function onClick() {
    if (!props.disabled)
        emit('update:modelValue', !props.modelValue);
}
</script>

<template>
    <button
        :class="{ 'icon-disabled': disabled }"
        @click="onClick"
    >
        <div
            class="d-flex align-items-center"
            style="height: 24px;"
        >
            <IconChevronUp
                v-if="modelValue"
                width="16"
                height="16"
            />
            <IconChevronDown
                v-else
                width="16"
                height="16"
            />
        </div>
    </button>
</template>

<style scoped>
.icon-disabled {
    color: var(--color-text);
    cursor: auto;
}
</style>
