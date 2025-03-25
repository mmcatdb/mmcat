<script setup lang="ts">
import { ref } from 'vue';
import type { Graph, Node } from '@/types/categoryGraph';
import Divider from '@/components/layout/Divider.vue';
import type { Candidates, ReferenceCandidate,  PrimaryKeyCandidate } from '@/types/inference/candidates';
import ReferenceMerge from '@/components/category/inference/ReferenceMerge.vue';
import PrimaryKeyMerge from '@/components/category/inference/PrimaryKeyMerge.vue';

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** The graph object used for merging operations. */
    graph: Graph;
    /** Candidates for merging, either by reference or primary key. */
    candidates: Candidates;
}>();

/**
 * Emits custom events to the parent component.
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
 */
function confirmReference(payload: Node[] | ReferenceCandidate ) {
    emit('confirm-reference-merge', payload);
}

/**
 * Confirms the primary key merge and emits the 'confirm-primary-key-merge' event.
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
</script>

<template>
    <div>
        <h3>
            Merge Objects
        </h3>

        <div class="mb-2 d-flex gap-4">
            <label class="d-flex align-items-center cursor-pointer">
                <input
                    v-model="mergeType"
                    type="radio"
                    value="reference"
                />
                Reference
            </label>
            <label class="d-flex align-items-center cursor-pointer">
                <input
                    v-model="mergeType"
                    type="radio"
                    value="primaryKey"
                />
                Primary Key
            </label>
        </div>

        <p
            v-if="mergeType === 'reference'"
            style="max-width: 300;"
        >
            <!-- FIXME -->
            Objects will be merged by ... (reference)
        </p>
        <p
            v-else-if="mergeType === 'primaryKey'"
            style="max-width: 300;"
        >
            Objects will be merged by ... (primaryKey)
        </p>

        <Divider class="my-3" />
        
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
