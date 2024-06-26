<script setup lang="ts">
import type { VersionId } from '@/types/id';

type VersionDisplayProps = {
    versionId: VersionId;
    error?: boolean;
};

const props = defineProps<VersionDisplayProps>();
</script>

<template>
    <span
        class="fw-semibold"
        :class="{ 'text-error': props.error }"
    >
        {{ props.versionId }}
    </span>
</template>
