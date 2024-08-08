<script setup lang="ts">
import { shallowRef } from 'vue';
import Merge from './Merge.vue';
import Cluster from './Cluster.vue';
import Recursion from './Recursion.vue';
import InferenceEdits from './InferenceEdits.vue';
import type { AbstractInferenceEdit } from '@/types/inferenceEdit/inferenceEdit';
import { Graph, Node, Edge } from '@/types/categoryGraph';
import { SchemaCategory } from '@/types/schema';
import Divider from '@/components/layout/Divider.vue';

const props = defineProps<{
    graph: Graph;
    schemaCategory: SchemaCategory;
    inferenceEdits: AbstractInferenceEdit[];
}>();

const emit = defineEmits<{
    (e: 'cancel-edit'): void;
    (e: 'confirm-reference-merge', nodes: (Node)[]): void;
    (e: 'confirm-primary-key-merge', nodes: (Node)[]): void;
    (e: 'confirm-cluster', nodes: (Node)[]): void;
    (e: 'confirm-recursion', payload: { nodes: (Node)[], edges: (Edge)[] }): void;
    (e: 'revert-edit', edit: AbstractInferenceEdit): void;
}>();

enum State {
    Default,
    Merge,
    Cluster,
    Recursion,
    Edits,
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = 
    GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.Merge, unknown> |
    GenericStateValue<State.Cluster, unknown> |
    GenericStateValue<State.Recursion, unknown> |
    GenericStateValue<State.Edits, unknown>;

const state = shallowRef<StateValue>({ type: State.Default });

function editsClicked() {
    state.value = { type: State.Edits };
}

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

function confirmReferenceMergeEdit(nodes: (Node)[]) {
    emit('confirm-reference-merge', nodes);
}

function confirmPrimaryKeyMergeEdit(nodes: (Node)[]) {
    emit('confirm-primary-key-merge', nodes);
}

function confirmClusterEdit(nodes: (Node)[]) {
    emit('confirm-cluster', nodes);
}

function confirmRecursionEdit(nodes: (Node)[], edges: (Edge)[]) {
    emit('confirm-recursion', { nodes, edges });
}

function cancelEdit() {
    emit('cancel-edit');
}

function revertEdit(edit: AbstractInferenceEdit) {
    emit('revert-edit', edit);
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
            <Divider />
            <button @click="editsClicked">
                Edits
            </button>
        </div>
        <template v-else-if="state.type === State.Merge">
            <Merge
                :graph="props.graph"
                @confirm-reference-merge="confirmReferenceMergeEdit"
                @confirm-primary-key-merge="confirmPrimaryKeyMergeEdit"
                @cancel="setStateToDefault"
                @cancel-edit="cancelEdit"
            />
        </template>
        <template v-else-if="state.type === State.Cluster">
            <Cluster
                :graph="props.graph"
                @confirm="confirmClusterEdit"
                @cancel="setStateToDefault"
                @cancel-edit="cancelEdit"
            />
        </template>
        <template v-else-if="state.type === State.Recursion">
            <Recursion
                :graph="props.graph"
                @confirm="confirmRecursionEdit"
                @cancel="setStateToDefault"
                @cancel-edit="cancelEdit"
            />
        </template>
        <template v-else-if="state.type === State.Edits">
            <InferenceEdits
                :inference-edits="props.inferenceEdits"
                @cancel="setStateToDefault"
                @revert-edit="revertEdit"
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
