<script setup lang="ts">
import { ref } from 'vue';
import type { Graph } from '@/types/categoryGraph';
import Divider from '@/components/layout/Divider.vue';
import AddProperty from './AddProperty.vue';

enum State {
    Default,
    AddProperty,
}

type AddComplexStructureProps = {
    graph: Graph;
};

defineProps<AddComplexStructureProps>();

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
        <Divider />
        <button @click="cancel">
            Cancel
        </button>
    </div>
    <template v-else-if="state === State.AddProperty">
        <AddProperty
            :graph="graph"
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
