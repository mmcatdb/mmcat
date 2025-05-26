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
import PrimaryKey from './PrimaryKey.vue';

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
    (e: 'confirm-merge', payload: Node[] | ReferenceCandidate): void;
    (e: 'confirm-primary-key', nodes: Node[] | PrimaryKeyCandidate): void;
    (e: 'confirm-cluster', nodes: Node[]): void;
    (e: 'confirm-recursion', payload: { nodes: Node[], edges: Edge[] }): void;
    (e: 'revert-edit', edit: InferenceEdit): void;
    (e: 'update-position', key: Key, newPosition: Position): void;
}>();

/**
 * Enum representing the different states of the editor.
 */
enum State {
    Default,
    Merge,
    PrimaryKey,
    Cluster,
    Recursion,
    Edits,
}

/**
 * Generic state value type used to manage the current state of the editor.
 */
type GenericStateValue<State, Value> = { type: State } & Value;

/**
 * State value representing the current state of the editor.
 */
type StateValue = 
    GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.Merge, unknown> |
    GenericStateValue<State.PrimaryKey, unknown> |
    GenericStateValue<State.Cluster, unknown> |
    GenericStateValue<State.Recursion, unknown> |
    GenericStateValue<State.Edits, unknown>;

/**
 * Tracks the current state of the editor.
 */
const state = shallowRef<StateValue>({ type: State.Default });

/**
 * Resets the state to 'Default'.
 */
function setStateToDefault() {
    state.value = { type: State.Default };
}

/**
 * Emits the 'confirm-merge' event when a reference merge is confirmed.
 */
function confirmReferenceMergeEdit(payload: Node[] | ReferenceCandidate) {
    console.log('i want to confirm this edit');
    emit('confirm-merge', payload);
}

/**
 * Emits the 'confirm-primary-key' event when a primary key merge is confirmed.
 */
function confirmPrimaryKeyMergeEdit(payload: Node[] | PrimaryKeyCandidate) {
    emit('confirm-primary-key', payload);
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

onMounted(() => {
    props.graph.listen()?.onNode('dragfreeon', node => {
        emit('update-position', node.objex.key, { ...node.cytoscapeIdAndPosition.position });
    });
});
</script>

<template>
    <div
        class="editor w-fit"
        style="min-width: 200px;"
    >
        <div
            v-if="state.type === State.Default"
            class="options"
        >
            <h3 class="mb-3">
                Actions
            </h3>
        
            <button @click="state = { type: State.Merge }">
                Merge
            </button>
            <button @click="state = { type: State.PrimaryKey }">
                Primary key
            </button>
            <button @click="state = { type: State.Cluster }">
                Cluster
            </button>
            <button @click="state = { type: State.Recursion }">
                Recursion
            </button>
           
            <Divider />
            
            <button @click="state = { type: State.Edits }">
                Edits
            </button>
        </div>
        <Merge
            v-else-if="state.type === State.Merge"
            :graph="props.graph"
            :candidates="props.candidates"
            @confirm="confirmReferenceMergeEdit"
            @cancel="setStateToDefault"
            @cancel-edit="cancelEdit"
        />
        <PrimaryKey
            v-else-if="state.type === State.PrimaryKey"
            :graph="props.graph"
            :candidates="props.candidates"
            @confirm="confirmPrimaryKeyMergeEdit"
            @cancel="setStateToDefault"
            @cancel-edit="cancelEdit"
        />
        <Cluster
            v-else-if="state.type === State.Cluster"
            :graph="props.graph"
            @confirm="confirmClusterEdit"
            @cancel="setStateToDefault"
            @cancel-edit="cancelEdit"
        />
        <Recursion
            v-else-if="state.type === State.Recursion"
            :graph="props.graph"
            @confirm="confirmRecursionEdit"
            @cancel="setStateToDefault"
            @cancel-edit="cancelEdit"
        />
        <InferenceEdits
            v-else-if="state.type === State.Edits"
            :inference-edits="props.inferenceEdits"
            @cancel="setStateToDefault"
            @revert-edit="revertEdit"
        />
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
