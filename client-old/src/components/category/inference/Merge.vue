<script setup lang="ts">
import { ref } from 'vue';
import type { Graph, Node } from '@/types/categoryGraph';
import Divider from '@/components/layout/Divider.vue';
import ReferenceMerge from '@/components/category/inference/ReferenceMerge.vue';
import PrimaryKeyMerge from '@/components/category/inference/PrimaryKeyMerge.vue';

const props = defineProps<{
    graph: Graph;
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
            <label>
                <input
                    v-model="mergeType"
                    type="radio"
                    value="reference"
                /> Reference
            </label>
            <label>
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
            @confirm="confirmReference"
            @cancel="cancel"
            @cancel-edit="cancelEdit"
        />
        <PrimaryKeyMerge
            v-else-if="mergeType === 'primaryKey'"
            :graph="props.graph"
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
