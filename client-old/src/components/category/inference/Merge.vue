<script setup lang="ts">
import { ref } from 'vue';
import type { Graph, Node } from '@/types/categoryGraph';
import Divider from '@/components/layout/Divider.vue';
import { Candidates, ReferenceCandidate, PrimaryKeyCandidate } from '@/types/inference/candidates';
import ReferenceMerge from '@/components/category/inference/ReferenceMerge.vue';
import PrimaryKeyMerge from '@/components/category/inference/PrimaryKeyMerge.vue';

const props = defineProps<{
    graph: Graph;
    candidates: Candidates;
}>();

const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm-reference-merge', payload: Node[] | ReferenceCandidate): void;
    (e: 'confirm-primary-key-merge', nodes: Node[] | PrimaryKeyCandidate): void;
}>();

const mergeType = ref<'reference' | 'primaryKey'>('reference');

function confirmReference(payload: Node[] | ReferenceCandidate ) {
    emit('confirm-reference-merge', payload);
}

function confirmPrimaryKey(payload: Node[] | PrimaryKeyCandidate) {
    emit('confirm-primary-key-merge', payload);
}

function cancel() {
    emit('cancel');
}

function cancelEdit() {
    emit('cancel-edit');
}

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

<style>
.number-input {
    max-width: 80px;
}

.button-row {
    display: flex;
    gap: 10px;
    justify-content: center;
}

</style>
