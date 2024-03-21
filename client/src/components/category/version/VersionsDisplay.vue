<script setup lang="ts">
import { computed, watch } from 'vue';

import { useEvocat } from '@/utils/injects';

import { computeLatestVersions, type Version } from '@/types/evocat/Version';

type VersionsDisplayProps = {
    allVersions: Version[];
    currentVersion: Version;
    showAll?: boolean;
};

const props = withDefaults(defineProps<VersionsDisplayProps>(), {
    showAll: false,
});

const latestVersions = computed(() => computeLatestVersions(props.allVersions[0]));

const lastLatestVersion = computed(() => latestVersions.value[latestVersions.value.length - 1]);

const { evocat } = $(useEvocat());

// If we want to hide the older branches, but the current version won't be shown, we pick the latest version as the current one.
watch(() => props.showAll, (newValue: boolean) => {
    if (!newValue && !latestVersions.value.find(version => version.id === props.currentVersion.id))
        evocat.move(lastLatestVersion.value);
}, { immediate: true });

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
    previousVersions.reverse();

    const nextVersions: Version[] = [];

    nextVersion = props.currentVersion.lastChild;
    level = props.currentVersion.level;
    while (nextVersion) {
        if (nextVersion.level <= level)
            nextVersions.push(nextVersion);

        level = Math.min(level, nextVersion.level);
        nextVersion = nextVersion.lastChild;
    }

    const output = [ ...previousVersions, props.currentVersion, ...nextVersions ];

    if (lastLatestVersion.value.level > 0) {
        const lastOutput = output[output.length - 1];
        const tailVersions = [];
        nextVersion = lastLatestVersion.value;

        while (nextVersion && nextVersion.id !== lastOutput.id) {
            tailVersions.push(nextVersion);
            nextVersion = nextVersion.parent;
        }

        tailVersions.reverse();
        output.push(...tailVersions);
    }

    return output;
});
</script>

<template>
    <div class="d-flex flex-wrap align-items-start row-gap-3 column-gap-2">
        <div
            v-for="version in (showAll ? allVersions : displayedVersions)"
            :key="version.id"
            class="version-display monospace-font text-center"
            :class="{
                active: version.id === currentVersion.id,
                clickable: version.id !== currentVersion.id,
                wrapper: version.isCompositeWrapper,
                'show-all': showAll,
            }"
            :style="{ marginTop: `${8 * version.level}px` }"
            @click="() => version.id !== currentVersion.id && evocat.move(version)"
        >
            {{ showAll ? version.id : version.branchlessId }}
        </div>
    </div>
</template>

<style scoped>
.version-display {
    width: 48px;
    line-height: 26px;
    border: 1px solid var(--color-text);
    border-radius: 3px;
    position: relative;
}

.version-display.show-all {
    width: 64px;
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
