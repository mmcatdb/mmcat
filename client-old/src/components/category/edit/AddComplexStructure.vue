<script setup lang="ts">
import { ref } from 'vue';
import Divider from '@/components/layout/Divider.vue';
import AddProperty from './AddProperty.vue';
import AddSet from './AddSet.vue';
import AddMap from './AddMap.vue';
import FinishCompositeOperation from './FinishCompositeOperation.vue';

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
        <!-- 
        TODO The button should be disabled if the current version is the root version.
        This will be much easier in the react UI.
        :disabled="currentVersion.level === 0"*
        -->
        <button
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
