<script setup lang="ts">
import type { Version } from '@/types/id';

type VersionDisplayProps = {
    version: Version;
    error?: boolean;
};

const props = defineProps<VersionDisplayProps>();
</script>

<template>
    <span
        :class="{ 'text-error': props.error }"
    >
        {{ props.version }}
    </span>
</template>
