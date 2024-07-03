<script setup lang="ts">

const props = withDefaults(defineProps<{ disabled?: boolean }>(), {
    disabled: false,
});

const emit = defineEmits([ 'click' ]);

function onClick() {
    if (!props.disabled)
        emit('click');
}
</script>

<template>
    <span
        class="button-icon"
        :class="{ 'icon-disabled': disabled }"
        @click="onClick"
    >
        <slot />
    </span>
</template>

<style scoped>
.icon-disabled {
    color: var(--color-text);
    cursor: auto;
}
</style>
