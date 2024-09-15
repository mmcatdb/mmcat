<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, shallowRef } from 'vue';

import { useEvocat } from '@/utils/injects';

// import type { Version } from '@/types/evocat/Version';
import VersionsDisplay from './VersionsDisplay.vue';
import IconArrowLeftBold from '@/components/icons/IconArrowLeftBold.vue';
import IconArrowRightBold from '@/components/icons/IconArrowRightBold.vue';
import IconArrowUpBold from '@/components/icons/IconArrowUpBold.vue';
import IconArrowDownBold from '@/components/icons/IconArrowDownBold.vue';

const { evocat } = $(useEvocat());

const allVersions = shallowRef<Version[]>(evocat.versionContext.allVersions);
const currentVersion = shallowRef<Version>(evocat.versionContext.currentVersion);

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

const showAll = ref(false);
</script>

<template>
    <div class="versions-control p-3 ps-2 d-flex align-items-start gap-3">
        <div>
            <div class="d-flex gap-2">
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
            <div class="py-2 text-center">
                <input
                    v-model="showAll"
                    type="checkbox"
                    style="margin-right: 8px;"
                />
                <span>Show all?</span>
            </div>
        </div>
        <VersionsDisplay
            :current-version="currentVersion"
            :all-versions="allVersions"
            :show-all="showAll"
        />
    </div>
</template>

<style scoped>
.versions-control {
    width: var(--schema-category-canvas-width);
}

.button {
    padding: 0px 4px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}
</style>
