<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits([ 'save', 'cancel' ]);

const label = ref('');
const keyIsValid = ref(true);

function save() {
    emit('save');
}

function cancel() {
    emit('cancel');
}
</script>

<template>
       <div>
        <h2>Merge Schema Categories</h2>
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