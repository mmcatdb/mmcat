<script setup lang="ts">
import { ref } from 'vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { useEvocat } from '@/utils/injects';

const { evocat } = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const label = ref('');
const keyIsValid = ref(true);

function save() {
    evocat.createObjex({ label: label.value });
    emit('save');
}

function cancel() {
    emit('cancel');
}
</script>

<template>
    <div>
        <h2>Add Schema Object</h2>
        <ValueContainer>
            <ValueRow label="Label:">
                <input v-model="label" />
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="!keyIsValid || !label"
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
