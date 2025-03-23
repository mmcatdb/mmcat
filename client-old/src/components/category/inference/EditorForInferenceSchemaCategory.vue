<script setup lang="ts">
import { shallowRef, onMounted } from 'vue';
import Merge from './Merge.vue';
import Cluster from './Cluster.vue';
import Recursion from './Recursion.vue';
import InferenceEdits from './InferenceEdits.vue';
import type { InferenceEdit } from '@/types/inference/inferenceEdit';
import type { Candidates, ReferenceCandidate, PrimaryKeyCandidate } from '@/types/inference/candidates';
import type { Graph, Node, Edge } from '@/types/categoryGraph';
import type { Category } from '@/types/schema';
import Divider from '@/components/layout/Divider.vue';
import type { Key } from '@/types/identifiers';
import type { Position } from 'cytoscape';

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** The graph instance used in the component. */
    graph: Graph;
    /** The schema category. */
    schemaCategory: Category;
    /** Array of inference edits. */
    inferenceEdits: InferenceEdit[];
    /** The candidates for merging. */
    candidates: Candidates;
}>();

/**
 * Emits custom events to the parent component.
 */
const emit = defineEmits<{
    (e: 'cancel-edit'): void;
    (e: 'confirm-reference-merge', payload: Node[] | ReferenceCandidate): void;
    (e: 'confirm-primary-key-merge', nodes: Node[] | PrimaryKeyCandidate): void;
    (e: 'confirm-cluster', nodes: Node[]): void;
    (e: 'confirm-recursion', payload: { nodes: Node[], edges: Edge[] }): void;
    (e: 'revert-edit', edit: InferenceEdit): void;
    (e: 'save-positions', map: Map<Key, Position>): void;
}>();

/**
 * Enum representing the different states of the editor.
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
 */
type GenericStateValue<State, Value> = { type: State } & Value;

const updatedPositionsMap = new Map<Key, Position>();

/**
 * State value representing the current state of the editor.
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

onMounted(() => {
    props.graph.listen()?.onNode('dragfreeon', node => {
        const newPosition = { ...node.cytoscapeIdAndPosition.position };
        updatedPositionsMap.set(node.object.key, newPosition);
    });
});

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
 */
function confirmReferenceMergeEdit(payload: Node[] | ReferenceCandidate) {
    emit('confirm-reference-merge', payload);
}

/**
 * Emits the 'confirm-primary-key-merge' event when a primary key merge is confirmed.
 */
function confirmPrimaryKeyMergeEdit(payload: Node[] | PrimaryKeyCandidate) {
    emit('confirm-primary-key-merge', payload);
}

/**
 * Emits the 'confirm-cluster' event when a cluster operation is confirmed.
 */
function confirmClusterEdit(nodes: Node[]) {
    emit('confirm-cluster', nodes);
}

/**
 * Emits the 'confirm-recursion' event when a recursion operation is confirmed.
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
 */
function revertEdit(edit: InferenceEdit) {
    emit('revert-edit', edit);
}

/**
 * Emits the 'save-positions' even when positions are saved.
 */
function savePositions() {
    if (updatedPositionsMap.size > 0) 
        emit('save-positions', updatedPositionsMap);
        //updatedPositionsMap.clear();
    
}

</script>

<template>
    <div class="editor border-top-0">
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
            <Divider />
            <button @click="savePositions">
                Save Positions
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
