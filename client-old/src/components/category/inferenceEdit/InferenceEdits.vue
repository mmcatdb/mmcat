<script setup lang="ts">
import { computed } from 'vue';
import type { AbstractInferenceEdit } from '@/types/inferenceEdit/inferenceEdit';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import Divider from '@/components/layout/Divider.vue';

const props = defineProps<{
    inferenceEdits: AbstractInferenceEdit[];
}>();

const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'undo-edit', edit: AbstractInferenceEdit): void;
    (e: 'redo-edit', edit: AbstractInferenceEdit): void;
}>();

const hasEdits = computed(() => props.inferenceEdits.length > 0);

function undoEdit(edit: AbstractInferenceEdit) {
    emit('undo-edit', edit);
}

function redoEdit(edit: AbstractInferenceEdit) {
    emit('redo-edit', edit);
}

function cancel() {
    emit('cancel');
}

function getEditName(editType: string): string {
    switch (editType) {
    case 'primaryKey':
        return 'Primary Key Merge';
    case 'reference':
        return 'Reference Merge';
    case 'recursion':
        return 'Recursion';
    case 'cluster':
        return 'Cluster';
    default:
        return 'Unknown';
    }
}


</script>

<template>
    <div class="edits">
        <h2>Edits</h2>
        <ValueContainer>
            <ValueRow> 
                <template v-if="hasEdits">
                    <div v-for="(edit, index) in props.inferenceEdits" :key="index" class="edit-item">
                        <p>
                            <strong>{{ getEditName(edit.type) }}</strong>: 
                            id: {{ edit.id }}, 
                            active: {{ edit.isActive }}
                        </p>
                        <button 
                            @click="undoEdit(edit)"
                            :disabled="!edit.isActive"
                        >
                            Undo
                        </button>
                        <button
                            @click="redoEdit(edit)"
                            :disabled="edit.isActive"
                        >
                            Redo
                        </button>
                        <Divider />
                        <br>
                    </div>
                </template>
                <template v-else>
                    <p>No edits available</p>
                </template>
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>