<script setup lang="ts">
import { onMounted, onUnmounted, shallowRef } from 'vue';
import Merge from './Merge.vue';
import { Graph, Node } from '@/types/categoryGraph';
import { SchemaCategory } from '@/types/schema';

const props = defineProps<{
    graph: Graph;
    schemaCategory: SchemaCategory;
}>();

enum State {
    Default,
    Merge,
    Cluster,
    Recursion,
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = 
    GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.Merge, unknown> |
    GenericStateValue<State.Cluster, unknown> |
    GenericStateValue<State.Recursion, unknown>;

const state = shallowRef<StateValue>({ type: State.Default });

const listener = props.graph.listen();

onMounted(() => {
    listener.onNode('tap', onNodeTapHandler);
})

onUnmounted(() => {
    listener.close();
})

function mergeClicked() {
    state.value = { type: State.Merge };
}

function clusterClicked() {
    state.value = { type: State.Cluster };
}

function recursionClicked() {
    state.value = { type: State.Recursion };
}

function setStateToDefault() {
    state.value = { type: State.Default };
}

function onNodeTapHandler(node: Node) {
    console.log("Node clicked", node);
}



</script>

<template>
        <div class="editor">
        <div
            v-if="state.type === State.Default"
            class="options"
        >
            <button @click="mergeClicked">
                Merge
            </button>
            <button @click="clusterClicked">
                Cluster
            </button>
            <button @click="recursionClicked">
                Recursion
            </button>
        </div>
        <template v-else-if="state.type === State.Merge">
            <Merge
                :graph="props.graph"
                @confirm=""
                @save="setStateToDefault"
                @cancel="setStateToDefault"
            />
        </template>
    </div>

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
