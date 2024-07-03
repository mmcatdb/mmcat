<script setup lang="ts">
import { ref } from 'vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { useEvocat } from '@/utils/injects';

const { evocat } = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const operationLabel = ref('');

function save() {
    evocat.finishCompositeOperation(operationLabel.value);
    emit('save');
}

function cancel() {
    emit('cancel');
}
</script>

<template>
    <div>
        <h2>Finish operation</h2>
        <ValueContainer>
            <ValueRow label="Label:">
                <input v-model="operationLabel" />
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="!operationLabel"
                @click="save"
            >
                Confirm
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>
