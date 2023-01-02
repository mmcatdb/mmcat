<script setup lang="ts">
interface ResourceNotFoundProps {
    reloadable?: boolean
}

const props = defineProps<ResourceNotFoundProps>();

const emit = defineEmits([ 'reload' ]);
</script>

<template>
    <p>
        Resource not found.
        <button
            v-if="props.reloadable"
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
