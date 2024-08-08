<script setup lang="ts">
import { ref, computed } from 'vue';
import { Graph } from '@/types/categoryGraph';
import Divider from '@/components/layout/Divider.vue';
import { Node } from '@/types/categoryGraph';
import ReferenceMerge from '@/components/category/inferenceEdit/ReferenceMerge.vue';
import PrimaryKeyMerge from '@/components/category/inferenceEdit/PrimaryKeyMerge.vue';

const props = defineProps<{
    graph: Graph;
}>();

const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm-reference-merge', nodes: (Node)[]): void;
    (e: 'confirm-primary-key-merge', nodes: (Node)[]): void;
}>();

const mergeType = ref<'reference' | 'primaryKey'>('reference');

const confirmHandler = computed(() => {
    return mergeType.value === 'reference' ? confirmReference : confirmPrimaryKey;
});

function confirmReference(nodes: (Node)[]) {
    emit('confirm-reference-merge', nodes);
}

function confirmPrimaryKey(nodes: (Node)[]) {
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
        <component
            :is="mergeType === 'reference' ? ReferenceMerge : PrimaryKeyMerge"
            :graph="props.graph"
            @confirm="confirmHandler"
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
