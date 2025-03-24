<script setup lang="ts">
import { computed } from 'vue';
import type { InferenceEdit } from '@/types/inference/inferenceEdit';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
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
    <div class="edits">
        <h3 class="edits-title">
            Edits
            <span class="tooltip-container">
                <span class="question-mark">?</span>
                <span class="tooltip-text">
                View a list of all edits you have applied so far.<br>
                </span>
            </span> 
        </h3>
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
                Back
            </button>
        </div>
    </div>
</template>

<style>
.merge-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tooltip-container {
  position: relative;
  display: inline-block;
  flex-shrink: 0;
}

.question-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  font-size: 11px;
  border-radius: 50%;
  background-color: #999;
  color: white;
  font-weight: bold;
  cursor: default;
}

.tooltip-text {
  visibility: hidden;
  opacity: 0;
  position: absolute;
  bottom: 125%;
  left: 50%;
  transform: translateX(-50%);
  width: max-content;
  max-width: 220px;
  padding: 6px 8px;
  font-size: 12px;
  background-color: #333;
  color: #fff;
  text-align: left;
  border-radius: 6px;
  z-index: 1;
  pointer-events: none;
  transition: opacity 0.2s ease-in-out;
}

.tooltip-container:hover .tooltip-text {
  visibility: visible;
  opacity: 1;
}
</style>
