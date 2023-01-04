<script setup lang="ts">
import type { SchemaObject } from '@/types/schema';
import type { Signature } from '@/types/identifiers/Signature';

export type Column = {
    signature: Signature;
    schemaObject: SchemaObject | undefined;
    isClickable: boolean;
}

interface InstanceObjectHeaderProps {
    showTechnicalIds: boolean;
    columns: Column[];
}

defineProps<InstanceObjectHeaderProps>();

const emit = defineEmits([ 'object:click' ]);

function columnClicked(column: Column) {
    if (column.isClickable)
        emit('object:click', column.schemaObject);
}
</script>

<template>
    <th v-if="showTechnicalIds">
        <span class="value">#</span>
    </th>
    <th
        v-for="column in columns"
        :key="column.signature.toString()"
        :class="{ clickable: column.isClickable }"
        @click="() => columnClicked(column)"
    >
        <span class="text-bold">
            {{ column.schemaObject?.label }}
        </span>
        <br />
        <span class="signature-span">
            {{ column.signature }}
        </span>
    </th>
</template>

<style scoped>
td, th {
    padding: 0 16px;
    background-color: var(--color-background-mute);
}
</style>
