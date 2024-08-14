<script setup lang="ts">
import { ref } from 'vue';
import type { Graph, Node } from '@/types/categoryGraph';
import Divider from '@/components/layout/Divider.vue';
import { Candidates } from '@/types/inference/candidates';
import ReferenceMerge from '@/components/category/inference/ReferenceMerge.vue';
import PrimaryKeyMerge from '@/components/category/inference/PrimaryKeyMerge.vue';

const props = defineProps<{
    graph: Graph;
    candidates: Candidates;
}>();

const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm-reference-merge', nodes: Node[]): void;
    (e: 'confirm-primary-key-merge', nodes: Node[]): void;
}>();

const mergeType = ref<'reference' | 'primaryKey'>('reference');

function confirmReference(nodes: Node[]) {
    emit('confirm-reference-merge', nodes);
}

function confirmPrimaryKey(nodes: Node[]) {
    emit('confirm-primary-key-merge', nodes);
}

function cancel() {
    emit('cancel');
}

function cancelEdit() {
    emit('cancel-edit');
}

</script>

<template>
    <div class="merge">
        <h2>Merge Objects</h2>
        <div class="merge-type">
            <label class="radio-label">
                <input
                    v-model="mergeType"
                    type="radio"
                    value="reference"
                /> Reference
            </label>
            <label class="radio-label">
                <input
                    v-model="mergeType"
                    type="radio"
                    value="primaryKey"
                /> Primary Key
            </label>
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

.radio-label {
    font-weight: bold; /* Make the text bold */
    font-size: 18px;   /* Adjust the size of the text */
    margin-right: 15px; /* Space between the radio buttons */
    cursor: pointer;   /* Change cursor to pointer for better UX */
}

.radio-label input[type="radio"] {
    margin-right: 5px; /* Space between the radio button and the label text */
}
</style>
