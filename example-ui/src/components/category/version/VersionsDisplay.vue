<script setup lang="ts">
import { computed } from 'vue';

import { useEvocat } from '@/utils/injects';

import type { Version } from '@/types/evocat/Version';

type VersionsDisplayProps = {
    allVersions: Version[];
    currentVersion: Version;
};

const props = defineProps<VersionsDisplayProps>();

const latestVersions = computed(() => {
    const output: Version[] = [];
    let nextVersion: Version | undefined = props.allVersions[0];

    while (nextVersion) {
        output.push(nextVersion);
        nextVersion = nextVersion.lastChild;
    }

    return output;
});

const lastLatestVersion = computed(() => latestVersions.value[latestVersions.value.length - 1]);

const displayedVersions = computed(() => {
    const previousVersions: Version[] = [];
    let nextVersion = props.currentVersion.parent;
    let level = props.currentVersion.level;
    while (nextVersion) {
        if (nextVersion.level <= level)
            previousVersions.push(nextVersion);

        level = Math.min(level, nextVersion.level);
        nextVersion = nextVersion.parent;
    }

    const nextVersions: Version[] = [];

    nextVersion = props.currentVersion.lastChild;
    level = props.currentVersion.level;
    while (nextVersion) {
        if (nextVersion.level <= level)
            nextVersions.push(nextVersion);

        level = Math.min(level, nextVersion.level);
        nextVersion = nextVersion.lastChild;
    }

    const output = [ ...previousVersions.reverse(), props.currentVersion, ...nextVersions ];

    if (lastLatestVersion.value.level > 0) {
        const lastOutput = output[output.length - 1];
        const tailVersions = [];
        nextVersion = lastLatestVersion.value;

        while (nextVersion && nextVersion.id !== lastOutput.id) {
            tailVersions.push(nextVersion);
            nextVersion = nextVersion.parent;
        }

        output.push(...tailVersions.reverse());
    }

    return output;
});

const { evocat } = $(useEvocat());

</script>

<template>
    <div class="versions-display">
        <div
            v-for="version in displayedVersions"
            :key="version.id"
            class="version-display monospace-font"
            :class="{
                active: version.id === currentVersion.id,
                clickable: version.id !== currentVersion.id,
                wrapper: version.isCompositeWrapper,
            }"
            :style="{ top: `${8 * version.level}px` }"
            @click="() => version.id !== currentVersion.id && evocat.move(version)"
        >
            {{ version.branchlessId }}
        </div>
    </div>
</template>

<style scoped>
.versions-display {
    display: flex;
    padding-left: 16px;
    padding-bottom: 8px;
    align-items: center;
    overflow-x: auto;
}

.version-display {
    padding: 2px 8px;
    border: 1px solid var(--color-text);
    border-radius: 3px;
    margin-right: 8px;
    position: relative;
}

.version-display.active {
    background-color: var(--color-text);
    color: var(--color-background);
    font-weight: bold;
}

.version-display.wrapper {
    outline-offset: 2px;
    outline: 1px solid white;
}
</style>
