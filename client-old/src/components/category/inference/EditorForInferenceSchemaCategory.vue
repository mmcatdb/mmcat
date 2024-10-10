<script setup lang="ts">
import { shallowRef } from 'vue';
import Merge from './Merge.vue';
import Cluster from './Cluster.vue';
import Recursion from './Recursion.vue';
import InferenceEdits from './InferenceEdits.vue';
import type { InferenceEdit } from '@/types/inference/inferenceEdit';
import { Candidates, ReferenceCandidate, PrimaryKeyCandidate } from '@/types/inference/candidates';
import { Graph, Node, Edge } from '@/types/categoryGraph';
import { SchemaCategory } from '@/types/schema';
import Divider from '@/components/layout/Divider.vue';

/**
 * Props passed to the component.
 * @typedef {Object} Props
 * @property {Graph} graph - The graph instance used in the component.
 * @property {SchemaCategory} schemaCategory - The schema category.
 * @property {InferenceEdit[]} inferenceEdits - Array of inference edits.
 * @property {Candidates} candidates - The candidates for merging.
 */
const props = defineProps<{
    graph: Graph;
    schemaCategory: SchemaCategory;
    inferenceEdits: InferenceEdit[];
    candidates: Candidates;
}>();

/**
 * Emits custom events to the parent component.
 * @emits cancel-edit - Emitted when the user cancels the edit.
 * @emits confirm-reference-merge - Emitted when a reference merge is confirmed.
 * @emits confirm-primary-key-merge - Emitted when a primary key merge is confirmed.
 * @emits confirm-cluster - Emitted when a cluster operation is confirmed.
 * @emits confirm-recursion - Emitted when a recursion operation is confirmed.
 * @emits revert-edit - Emitted when an inference edit is reverted.
 * @param {Node[] | ReferenceCandidate} payload - Payload for the merge operation.
 * @param {Node[] | PrimaryKeyCandidate} nodes - Nodes for the merge operation.
 * @param {{nodes: Node[], edges: Edge[]}} payload - Payload for the recursion operation.
 * @param {InferenceEdit} edit - The inference edit to revert.
 */
const emit = defineEmits<{
    (e: 'cancel-edit'): void;
    (e: 'confirm-reference-merge', payload: Node[] | ReferenceCandidate): void;
    (e: 'confirm-primary-key-merge', nodes: Node[] | PrimaryKeyCandidate): void;
    (e: 'confirm-cluster', nodes: Node[]): void;
    (e: 'confirm-recursion', payload: { nodes: Node[], edges: Edge[] }): void;
    (e: 'revert-edit', edit: InferenceEdit): void;
}>();

/**
 * Enum representing the different states of the editor.
 * @enum {number}
 */
enum State {
    Default,
    Merge,
    Cluster,
    Recursion,
    Edits,
}

/**
 * Generic state value type used to manage the current state of the editor.
 * @template State
 * @template Value
 * @typedef {Object} GenericStateValue
 * @property {State} type - The current state type.
 */
type GenericStateValue<State, Value> = { type: State } & Value;

/**
 * State value representing the current state of the editor.
 * @typedef {GenericStateValue<State.Default, unknown> | 
 * GenericStateValue<State.Merge, unknown> |
 * GenericStateValue<State.Cluster, unknown> |
 * GenericStateValue<State.Recursion, unknown> |
 * GenericStateValue<State.Edits, unknown>} StateValue
 */
type StateValue = 
    GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.Merge, unknown> |
    GenericStateValue<State.Cluster, unknown> |
    GenericStateValue<State.Recursion, unknown> |
    GenericStateValue<State.Edits, unknown>;

/**
 * Tracks the current state of the editor.
 */
const state = shallowRef<StateValue>({ type: State.Default });

/**
 * Sets the state to 'Edits' when the user clicks the edits button.
 */
function editsClicked() {
    state.value = { type: State.Edits };
}

/**
 * Sets the state to 'Merge' when the user clicks the merge button.
 */
function mergeClicked() {
    state.value = { type: State.Merge };
}

/**
 * Sets the state to 'Cluster' when the user clicks the cluster button.
 */
function clusterClicked() {
    state.value = { type: State.Cluster };
}

/**
 * Sets the state to 'Recursion' when the user clicks the recursion button.
 */
function recursionClicked() {
    state.value = { type: State.Recursion };
}

/**
 * Resets the state to 'Default'.
 */
function setStateToDefault() {
    state.value = { type: State.Default };
}

/**
 * Emits the 'confirm-reference-merge' event when a reference merge is confirmed.
 * @param {Node[] | ReferenceCandidate} payload - The payload for the reference merge.
 */
function confirmReferenceMergeEdit(payload: Node[] | ReferenceCandidate) {
    emit('confirm-reference-merge', payload);
}

/**
 * Emits the 'confirm-primary-key-merge' event when a primary key merge is confirmed.
 * @param {Node[] | PrimaryKeyCandidate} payload - The payload for the primary key merge.
 */
function confirmPrimaryKeyMergeEdit(payload: Node[] | PrimaryKeyCandidate) {
    emit('confirm-primary-key-merge', payload);
}

/**
 * Emits the 'confirm-cluster' event when a cluster operation is confirmed.
 * @param {Node[]} nodes - The selected nodes for clustering.
 */
function confirmClusterEdit(nodes: Node[]) {
    emit('confirm-cluster', nodes);
}

/**
 * Emits the 'confirm-recursion' event when a recursion operation is confirmed.
 * @param {Node[]} nodes - The nodes involved in the recursion.
 * @param {Edge[]} edges - The edges involved in the recursion.
 */
function confirmRecursionEdit(nodes: Node[], edges: Edge[]) {
    emit('confirm-recursion', { nodes, edges });
}

/**
 * Emits the 'cancel-edit' event when an edit is canceled.
 */
function cancelEdit() {
    emit('cancel-edit');
}

/**
 * Emits the 'revert-edit' event when an inference edit is reverted.
 * @param {InferenceEdit} edit - The edit to revert.
 */
function revertEdit(edit: InferenceEdit) {
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
                :candidates="props.candidates"
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
