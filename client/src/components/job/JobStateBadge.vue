<script setup lang="ts">
import { JobState } from '@/types/job';

type JobDisplayProps = {
    state: JobState;
};

defineProps<JobDisplayProps>();

const states: Record<JobState, { label: string, bg: string, text: string }> = {
    [JobState.Ready]: { label: 'Waiting', bg: 'secondary', text: 'white' },
    [JobState.Running]: { label: 'Running', bg: 'info', text: 'white' },
    [JobState.Failed]: { label: 'Failed', bg: 'danger', text: 'white' },
    [JobState.Canceled]: { label: 'Canceled', bg: 'warning', text: 'white' },
    [JobState.Finished]: { label: 'Finished', bg: 'success', text: 'white' },
};
</script>

<template>
    <span :class="`badge bg-${states[state].bg} text-${states[state].text}`">
        {{ states[state].label }}
    </span>
</template>
