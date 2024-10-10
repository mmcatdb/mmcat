<script setup lang="ts">
import { computed } from 'vue';
import type { InferenceEdit } from '@/types/inference/inferenceEdit';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import Divider from '@/components/layout/Divider.vue';

/**
 * Props passed to the component.
 * @typedef {Object} Props
 * @property {InferenceEdit[]} inferenceEdits - Array of inference edits.
 */
const props = defineProps<{
    inferenceEdits: InferenceEdit[];
}>();

/**
 * Emits custom events to the parent component.
 * @emits cancel - Emitted when the cancel button is clicked.
 * @emits revert-edit - Emitted when an edit is reverted.
 * @param {InferenceEdit} edit - The inference edit to revert.
 */
const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'revert-edit', edit: InferenceEdit): void;
}>();

/**
 * Computed property that checks if there are any inference edits.
 */
const hasEdits = computed(() => props.inferenceEdits.length > 0);

console.log('inference edits: ', props.inferenceEdits);

/**
 * Emits the 'revert-edit' event when an inference edit is reverted.
 * @param {InferenceEdit} edit - The inference edit to revert.
 */
function revertEdit(edit: InferenceEdit) {
    emit('revert-edit', edit);
}

/**
 * Emits the 'cancel' event to cancel the current operation.
 */
function cancel() {
    emit('cancel');
}

/**
 * Returns a human-readable name for the edit type.
 * @param {string} editType - The type of the edit (PrimaryKey, Reference, Recursion, Cluster).
 * @returns {string} - The human-readable name of the edit type.
 */
function getEditName(editType: string): string {
    switch (editType) {
    case 'PrimaryKey':
        return 'Primary Key Merge';
    case 'Reference':
        return 'Reference Merge';
    case 'Recursion':
        return 'Recursion Merge';
    case 'Cluster':
        return 'Cluster Merge';
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
                    <div
                        v-for="(edit, index) in props.inferenceEdits"
                        :key="index"
                        class="edit-item"
                    >
                        <p>
                            <strong>{{ getEditName(edit.type) }}</strong>: 
                            id: {{ edit.id }}, 
                            active: {{ edit.isActive }}
                        </p>
                        <button 
                            :disabled="!edit.isActive"
                            @click="revertEdit(edit)"
                        >
                            Undo
                        </button>
                        <button
                            :disabled="edit.isActive"
                            @click="revertEdit(edit)"
                        >
                            Redo
                        </button>
                        <Divider />
                        <br />
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

<style scoped>
</style>
