<script setup lang="ts">
import type { Node } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

defineProps<{ nodes: Node[] }>();

const emit = defineEmits([ 'cancel' ]);

function cancel() {
    emit('cancel');
}
</script>

<template>
    <div>
        <h2>Edit Object Group</h2>
        <ValueContainer>
            <ValueRow label="Objects:" />
            <ValueRow
                v-for="node in nodes"
                :key="node.schemaObject.key.value"
                label="-"
            >
                {{ node.schemaObject.label }}
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
