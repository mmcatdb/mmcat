<script setup lang="ts">
import type { Session } from '@/types/job';


type SessionSelectProps = {
    sessions: Session[];
    currentSession: Session | undefined;
};

// TODO add to cookies + enable creating new sessions

const props = defineProps<SessionSelectProps>();
</script>

<template>
    <span>
        {{ currentSession?.id }}
    </span>
</template>
