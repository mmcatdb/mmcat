<script setup lang="ts">
import { computed } from 'vue';
import type { InferenceEdit } from '@/types/inference/inferenceEdit';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import Divider from '@/components/layout/Divider.vue';

const props = defineProps<{
    inferenceEdits: InferenceEdit[];
}>();

const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'revert-edit', edit: InferenceEdit): void;
}>();

const hasEdits = computed(() => props.inferenceEdits.length > 0);

function revertEdit(edit: InferenceEdit) {
    emit('revert-edit', edit);
}

function cancel() {
    emit('cancel');
}

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
                    <div v-for="(edit, index) in props.inferenceEdits" :key="index" class="edit-item">
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