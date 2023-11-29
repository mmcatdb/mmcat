<script setup lang="ts">
import { ErrorType } from '@/types/query';

type QueryUpdateErrorDisplayProps = {
    type: ErrorType;
    count: number;
};

defineProps<QueryUpdateErrorDisplayProps>();

const types: Record<ErrorType, { label: string, bg: string, color: string }> = {
    [ErrorType.ParseError]: { label: 'Parse error', bg: 'danger', color: 'white' },
    [ErrorType.UpdateError]: { label: 'Update error', bg: 'danger', color: 'white' },
    [ErrorType.UpdateWarning]: { label: 'Update warning', bg: 'warning', color: 'white' },
};
</script>

<template>
    <span :class="`badge bg-${types[type].bg} text-${types[type].color}`">
        {{ types[type].label + (count > 1 ? ` (${count})` : '') }}
    </span>
</template>
