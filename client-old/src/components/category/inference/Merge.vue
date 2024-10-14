<script setup lang="ts">
import { ref } from 'vue';
import type { Graph, Node } from '@/types/categoryGraph';
import Divider from '@/components/layout/Divider.vue';
import { Candidates, ReferenceCandidate, PrimaryKeyCandidate } from '@/types/inference/candidates';
import ReferenceMerge from '@/components/category/inference/ReferenceMerge.vue';
import PrimaryKeyMerge from '@/components/category/inference/PrimaryKeyMerge.vue';

/**
 * Props passed to the component.
 * @typedef {Object} Props
 * @property {Graph} graph - The graph object used for merging operations.
 * @property {Candidates} candidates - Candidates for merging, either by reference or primary key.
 */
const props = defineProps<{
    graph: Graph;
    candidates: Candidates;
}>();

/**
 * Emits custom events to the parent component.
 * @emits cancel - Emitted when the cancel button is clicked.
 * @emits cancel-edit - Emitted when the current edit is canceled.
 * @emits confirm-reference-merge - Emitted when a reference merge is confirmed.
 * @emits confirm-primary-key-merge - Emitted when a primary key merge is confirmed.
 * @param {Node[] | ReferenceCandidate} payload - The payload for the reference merge.
 * @param {Node[] | PrimaryKeyCandidate} nodes - The payload for the primary key merge.
 */
const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm-reference-merge', payload: Node[] | ReferenceCandidate): void;
    (e: 'confirm-primary-key-merge', nodes: Node[] | PrimaryKeyCandidate): void;
}>();

/**
 * Tracks the current merge type (either 'reference' or 'primaryKey').
 */
const mergeType = ref<'reference' | 'primaryKey'>('reference');

/**
 * Confirms the reference merge and emits the 'confirm-reference-merge' event.
 * @param {Node[] | ReferenceCandidate} payload - The payload for the reference merge.
 */
function confirmReference(payload: Node[] | ReferenceCandidate ) {
    emit('confirm-reference-merge', payload);
}

/**
 * Confirms the primary key merge and emits the 'confirm-primary-key-merge' event.
 * @param {Node[] | PrimaryKeyCandidate} payload - The payload for the primary key merge.
 */
function confirmPrimaryKey(payload: Node[] | PrimaryKeyCandidate) {
    emit('confirm-primary-key-merge', payload);
}

/**
 * Cancels the current operation by emitting the 'cancel' event.
 */
function cancel() {
    emit('cancel');
}

/**
 * Cancels the current edit by emitting the 'cancel-edit' event.
 */
function cancelEdit() {
    emit('cancel-edit');
}

/**
 * Sets the current merge type to either 'reference' or 'primaryKey'.
 * @param {'reference' | 'primaryKey'} type - The type of merge to set.
 */
function setMergeType(type: 'reference' | 'primaryKey') {
    mergeType.value = type;
}

</script>

<template>
    <div class="merge">
        <h2>Merge Objects</h2>
        <div class="merge-type button-row">
            <button
                :disabled="mergeType === 'reference'"
                @click="setMergeType('reference')"
            >
                Reference
            </button>
            <button
                :disabled="mergeType === 'primaryKey'"
                @click="setMergeType('primaryKey')"
            >
                Primary Key
            </button>
        </div>
        <Divider />
        <ReferenceMerge
            v-if="mergeType === 'reference'"
            :graph="props.graph"
            :candidates="props.candidates"
            @confirm="confirmReference"
            @cancel="cancel"
            @cancel-edit="cancelEdit"
        />
        <PrimaryKeyMerge
            v-else-if="mergeType === 'primaryKey'"
            :graph="props.graph"
            :candidates="props.candidates"
            @confirm="confirmPrimaryKey"
            @cancel="cancel"
            @cancel-edit="cancelEdit"
        />
    </div>
</template>

<style scoped>
.button-row {
    display: flex;
    gap: 10px;
    justify-content: center;
}
</style>