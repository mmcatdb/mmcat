<script setup lang="ts">
import type { MetadataObjex, SchemaObjex } from '@/types/schema';
import type { Signature } from '@/types/identifiers/Signature';

export type Column = {
    signature: Signature;
    schemaObjex: SchemaObjex | undefined;
    metadata: MetadataObjex | undefined;
    isClickable: boolean;
};

defineProps<{
    showTechnicalIds: boolean;
    columns: Column[];
}>();

const emit = defineEmits([ 'objex:click' ]);

function columnClicked(column: Column) {
    if (column.isClickable)
        emit('objex:click', column.schemaObjex);
}
</script>

<template>
    <th v-if="showTechnicalIds">
        <span class="value">#</span>
    </th>
    <th
        v-for="column in columns"
        :key="column.signature.value"
        :class="{ clickable: column.isClickable }"
        @click="() => columnClicked(column)"
    >
        <span class="text-bold">
            {{ column.metadata?.label }}
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
