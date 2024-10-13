<script setup lang="ts">
import { ErrorType, type QueryEvolutionError } from '@/types/query';
import IconXSquare from '../icons/IconXSquare.vue';

type QueryEvolutionErrorDisplayProps = {
    error: QueryEvolutionError;
};

defineProps<QueryEvolutionErrorDisplayProps>();

const emit = defineEmits([ 'deleteError' ]);

const types: Record<ErrorType, { label: string, color: string }> = {
    [ErrorType.ParseError]: { label: 'Parse error', color: 'danger' },
    [ErrorType.UpdateError]: { label: 'Update error', color: 'danger' },
    [ErrorType.UpdateWarning]: { label: 'Update warning', color: 'warning' },
};
</script>

<template>
    <div
        :class="`text-${types[error.type].color} border-${types[error.type].color}`"
        class="d-flex gap-3 rounded px-3 py-2 border"
    >
        <div class="fw-bold text-nowrap">
            {{ types[error.type].label }}:
        </div>
        <div class="flex-grow-1">
            {{ error.message }}
        </div>
        <div
            class="d-flex align-items-center"
            style="height: 24px;"
        >
            <IconXSquare
                class="clickable"
                @click="() => emit('deleteError')"
            />
        </div>
    </div>
</template>
