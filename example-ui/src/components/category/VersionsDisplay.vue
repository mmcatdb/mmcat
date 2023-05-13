<script setup lang="ts">
import { onMounted, onUnmounted, shallowRef } from 'vue';

import { useEvocat } from '@/utils/injects';

import type { Version } from '@/types/evocat/Version';

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

function undo() {
    evocat.undo();
}

function redo() {
    evocat.redo();
}
</script>

<template>
    <div class="versions-display">
        <div class="button-panel">
            <button
                @click="undo"
            >
                Undo
            </button>
            <button
                @click="redo"
            >
                Redo
            </button>
        </div>
        <div class="versions">
            <div
                v-for="version in allVersions"
                :key="version.id"
                class="version-display"
                :class="{ active: version.id === currentVersion.id }"
            >
                {{ version }}
            </div>
        </div>
    </div>
</template>

<style scoped>
.versions-display {
    display: flex;
    padding: 8px;
}

.versions {
    display: flex;
    padding-left: 16px;
    align-items: center;
}

.version-display {
    padding: 2px 8px;
    border: 1px solid var(--color-text);
    border-radius: 3px;
    margin-right: 8px;
}

.version-display.active {
    background-color: var(--color-text);
    color: var(--color-background);
    font-weight: bold;
}
</style>
