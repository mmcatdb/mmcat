<script setup lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { ref } from 'vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { computed } from '@vue/reactivity';

type AddObjectProps = {
    graph: Graph;
};

const props = defineProps<AddObjectProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const label = ref('');
const iri = ref('');
const pimIri = ref('');
const keyIsValid = ref(true);

const iriIsAvailable = computed(() => props.graph.schemaCategory.iriIsAvailable(iri.value));

function save() {
    if (iri.value) {
        const object = props.graph.schemaCategory.createObjectWithIri(label.value, undefined, iri.value, pimIri.value);
        if (!object)
            return;

        props.graph.createNode(object, 'new');
    }
    else {
        const object = props.graph.schemaCategory.createObject(label.value);
        props.graph.createNode(object, 'new');
    }

    props.graph.layout();
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
