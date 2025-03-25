<script setup lang="ts">
import { computed } from 'vue';
import type { InferenceEdit } from '@/types/inference/inferenceEdit';
import Divider from '@/components/layout/Divider.vue';

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** Array of inference edits. */
    inferenceEdits: InferenceEdit[];
}>();

/**
 * Emits custom events to the parent component.
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
    <div>
        <h3>
            Edits
        </h3>
        <p>
            View a list of all edits you have applied so far.
        </p>

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
        <p
            v-else
            class="text-bold"
        >
            No edits yet!
        </p>

        <div class="button-row">
            <button
                @click="cancel"
            >
                Back
            </button>
        </div>
    </div>
</template>
