<script setup lang="ts">
import { JobState } from '@/types/job';

type JobDisplayProps = {
    state: JobState;
};

defineProps<JobDisplayProps>();

const states: Record<JobState, { label: string, bg: string, color: string }> = {
    [JobState.Paused]: { label: 'Paused', bg: 'warning', color: 'white' },
    [JobState.Ready]: { label: 'Waiting', bg: 'secondary', color: 'white' },
    [JobState.Running]: { label: 'Running', bg: 'info', color: 'white' },
    [JobState.Failed]: { label: 'Failed', bg: 'danger', color: 'white' },
    [JobState.Canceled]: { label: 'Canceled', bg: 'warning', color: 'white' },
    [JobState.Finished]: { label: 'Finished', bg: 'success', color: 'white' },
};
</script>

<template>
    <span :class="`badge bg-${states[state].bg} text-${states[state].color}`">
        {{ states[state].label }}
    </span>
</template>
