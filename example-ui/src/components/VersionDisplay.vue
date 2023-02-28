<script setup lang="ts">
import type { Version } from '@/types/id';

type VersionDisplayProps = {
    version: Version;
};

const props = defineProps<VersionDisplayProps>();
</script>

<template>
    <span
        class="version-display"
    >
        {{ props.version }}
    </span>
</template>

<style>
.version-display {

}
</style>
