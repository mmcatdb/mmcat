<script setup lang="ts">
type ResourceNotFoundProps = {
    notReloadable?: boolean;
};

const props = defineProps<ResourceNotFoundProps>();

const emit = defineEmits([ 'reload' ]);
</script>

<template>
    <p>
        Resource not found.
        <button
            v-if="!props.notReloadable"
            class="reload-button"
            @click="() => emit('reload')"
        >
            Reload
        </button>
    </p>
</template>

<style scoped>
.reload-button {
    margin: 12px;
}
</style>
