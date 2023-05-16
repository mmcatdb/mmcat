<script setup lang="ts">
import { computed, onMounted, onUnmounted, shallowRef } from 'vue';

import { useEvocat } from '@/utils/injects';

import type { Version } from '@/types/evocat/Version';
import VersionsDisplay from './VersionsDisplay.vue';
import IconArrowLeftBold from '@/components/icons/IconArrowLeftBold.vue';
import IconArrowRightBold from '@/components/icons/IconArrowRightBold.vue';
import IconArrowUpBold from '@/components/icons/IconArrowUpBold.vue';
import IconArrowDownBold from '@/components/icons/IconArrowDownBold.vue';

const { evocat } = $(useEvocat());

const allVersions = shallowRef<Version[]>(evocat.versionContext.allVersions);
const currentVersion = shallowRef<Version>(evocat.versionContext.currentVersion);

function allListener(versions: Version[]) {
    allVersions.value = versions;
}

function currentListener(version: Version) {
    currentVersion.value = version;
}

onMounted(() => {
    evocat.versionContext.addAllListener(allListener);
    evocat.versionContext.addCurrentListener(currentListener);
});

onUnmounted(() => {
    evocat.versionContext.removeAllListener(allListener);
    evocat.versionContext.removeCurrentListener(currentListener);
});

const compositeParent = computed(() => {
    let nextVersion = currentVersion.value.lastChild;

    while (nextVersion) {
        if (nextVersion.level < currentVersion.value.level)
            return nextVersion;

        nextVersion = nextVersion.lastChild;
    }

    return undefined;
});

function undo() {
    evocat.undo();
}

function redoUp() {
    if (!compositeParent.value)
        return;

    evocat.move(compositeParent.value);
}

function undoDown() {
    evocat.undo(false);
}

function redo() {
    evocat.redo();
}
</script>

<template>
    <div class="versions-control">
        <div class="button-panel">
            <button
                class="button"
                :disabled="!currentVersion.parent"
                @click="undo"
            >
                <IconArrowLeftBold />
            </button>
            <button
                class="button"
                :disabled="!compositeParent"
                @click="redoUp"
            >
                <IconArrowUpBold />
            </button>
            <button
                class="button"
                :disabled="!currentVersion.isCompositeWrapper"
                @click="undoDown"
            >
                <IconArrowDownBold />
            </button>
            <button
                class="button"
                :disabled="!currentVersion.lastChild"
                @click="redo"
            >
                <IconArrowRightBold />
            </button>
        </div>
        <VersionsDisplay
            :current-version="currentVersion"
            :all-versions="allVersions"
        />
    </div>
</template>

<style scoped>
.versions-control {
    display: flex;
    padding: 8px;
}

.button-panel {
    display: flex;
    max-height: 30px;
}

.button {
    padding: 0px 4px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}
</style>
