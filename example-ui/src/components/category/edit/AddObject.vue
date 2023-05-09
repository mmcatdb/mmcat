<script setup lang="ts">
import { ref } from 'vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { computed } from '@vue/reactivity';
import { useEvocat } from '@/utils/injects';

const evocat = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const label = ref('');
const iri = ref('');
const pimIri = ref('');
const keyIsValid = ref(true);

const iriIsAvailable = computed(() => evocat.graph.schemaCategory.iriIsAvailable(iri.value));

function save() {
    if (iri.value) {
        const object = evocat.graph.schemaCategory.createObjectWithIri(label.value, undefined, iri.value, pimIri.value);
        if (!object)
            return;

        evocat.graph.createNode(object, 'new');
    }
    else {
        const object = evocat.graph.schemaCategory.createObject(label.value);
        evocat.graph.createNode(object, 'new');
    }

    evocat.graph.layout();
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
            <ValueRow label="Iri?:">
                <input v-model="iri" />
            </ValueRow>
            <ValueRow label="Pim Iri?:">
                <input v-model="pimIri" />
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="!keyIsValid || !label || !iriIsAvailable"
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
