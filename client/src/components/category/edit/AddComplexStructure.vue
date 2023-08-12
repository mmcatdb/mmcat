<script setup lang="ts">
import { onMounted, onUnmounted, ref, shallowRef } from 'vue';
import Divider from '@/components/layout/Divider.vue';
import AddProperty from './AddProperty.vue';
import AddSet from './AddSet.vue';
import AddMap from './AddMap.vue';
import FinishCompositeOperation from './FinishCompositeOperation.vue';
import { useEvocat } from '@/utils/injects';
import type { Version } from '@/types/evocat/Version';

enum State {
    Default,
    AddProperty,
    AddSet,
    AddMap,
    FinishCompositeOperation,
}

const emit = defineEmits([ 'cancel' ]);

const state = ref(State.Default);

function setStateToDefault() {
    state.value = State.Default;
}

function selectType(newState: State) {
    state.value = newState;
}

function cancel() {
    emit('cancel');
}


const { evocat } = $(useEvocat());

// TODO add to some inject

const currentVersion = shallowRef<Version>(evocat.versionContext.currentVersion);

function currentListener(version: Version) {
    currentVersion.value = version;
}

onMounted(() => {
    evocat.versionContext.addCurrentListener(currentListener);
});

onUnmounted(() => {
    evocat.versionContext.removeCurrentListener(currentListener);
});
</script>

<template>
    <div
        v-if="state === State.Default"
        class="options"
    >
        <button @click="() => selectType(State.AddProperty)">
            Add property
        </button>
        <button @click="() => selectType(State.AddSet)">
            Add set
        </button>
        <button @click="() => selectType(State.AddMap)">
            Add map
        </button>
        <Divider />
        <button
            :disabled="currentVersion.level === 0"
            @click="() => selectType(State.FinishCompositeOperation)"
        >
            Finish operation
        </button>
        <Divider />
        <button @click="cancel">
            Cancel
        </button>
    </div>
    <template v-else-if="state === State.AddProperty">
        <AddProperty
            @save="setStateToDefault"
            @cancel="setStateToDefault"
        />
    </template>
    <template v-else-if="state === State.AddSet">
        <AddSet
            @save="setStateToDefault"
            @cancel="setStateToDefault"
        />
    </template>
    <template v-else-if="state === State.AddMap">
        <AddMap
            @save="setStateToDefault"
            @cancel="setStateToDefault"
        />
    </template>
    <template v-else-if="state === State.FinishCompositeOperation">
        <FinishCompositeOperation
            @save="setStateToDefault"
            @cancel="setStateToDefault"
        />
    </template>
</template>

<style scoped>
.options {
    display: flex;
    flex-direction: column;
}

.options button {
    margin-top: 6px;
    margin-bottom: 6px;
}

.options button:first-of-type {
    margin-top: 0px;
}

.options button:last-of-type {
    margin-bottom: 0px;
}
</style>
