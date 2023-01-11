<script setup lang="ts">
import type { Graph } from '@/types/categoryGraph';
import dataspecerAPI from '@/utils/api/dataspecerAPI';
import { ref } from 'vue';
import { addImportedToGraph, importDataspecer } from '@/utils/integration';
import type { ImportedDataspecer } from '@/types/integration';
import Divider from '@/components/layout/Divider.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

//const EXAMPLE_IRI = '537f6a7e-0883-4d57-a19c-f275bd28af9f';
//const EXAMPLE_IRI = 'f2480523-c3ee-4c3c-a36a-9483749bc0d6';
//const EXAMPLE_IRI = 'https://ofn.gov.cz/data-specification/c697d1a1-4df1-4292-a136-df593fb6a32b';
//const EXAMPLE_IRI = 'https://ofn.gov.cz/data-specification/968c3ca7-bc39-4ecc-92c3-ad105cb01a5e';
const EXAMPLE_IRI = 'https://ofn.gov.cz/data-specification/b15fa5dc-2de4-4689-a910-27040732ee3f';


type IntegrationProps = {
    graph: Graph;
};

const props = defineProps<IntegrationProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const iri = ref(EXAMPLE_IRI);
const importedDataspecer = ref<ImportedDataspecer>();
const fetched = ref(false);
const fetching = ref(false);
const error = ref('');

async function importPIM() {
    fetching.value = true;
    const result = await dataspecerAPI.getStoreForIri(iri.value);
    if (result.status) {
        const imported = importDataspecer(result.data);
        importedDataspecer.value = imported;
    }
    else {
        error.value = result.error;
    }

    fetched.value = true;
    fetching.value = false;
}

function save() {
    if (!importedDataspecer.value)
        return;

    addImportedToGraph(importedDataspecer.value, props.graph);

    emit('save');
}

function cancel() {
    emit('cancel');
}

function tryAgain() {
    fetched.value = false;
}
</script>

<template>
    <div>
        <h2>Integration</h2>
        <template v-if="!fetched">
            <p>
                Import project from Dataspecer!
            </p>
            <ValueContainer>
                <ValueRow label="IRI:">
                    <input
                        v-model="iri"
                        class="iri-input"
                    />
                </ValueRow>
                <ValueRow label="Example:">
                    {{ EXAMPLE_IRI }}
                </ValueRow>
            </ValueContainer>
        </template>
        <template v-else>
            <template v-if="importedDataspecer">
                <p>
                    Import successful! The following resources were found:
                </p>
                <ValueContainer>
                    <ValueRow label="Classes:">
                        {{ importedDataspecer.counts.classes }}
                    </ValueRow>
                    <ValueRow label="Attributes:">
                        {{ importedDataspecer.counts.attributes }}
                    </ValueRow>
                    <ValueRow label="Associations:">
                        {{ importedDataspecer.counts.associations }}
                    </ValueRow>
                </ValueContainer>
                <Divider />
                <p>
                    Do you want to create the objects and morphisms?
                </p>
                <ValueContainer>
                    <ValueRow label="Objects:">
                        {{ importedDataspecer.objects.length }}
                    </ValueRow>
                    <ValueRow label="Morphisms:">
                        {{ importedDataspecer.morphisms.length }}
                    </ValueRow>
                </ValueContainer>
            </template>
            <template v-else>
                {{ error }}
            </template>
        </template>
        <div class="button-row">
            <button
                v-if="!importedDataspecer && !fetched"
                :disabled="!iri || fetching"
                @click="importPIM"
            >
                Import
            </button>
            <button
                v-if="importedDataspecer"
                @click="save"
            >
                Create all
            </button>
            <button
                v-if="fetched && !importedDataspecer || fetching"
                @click="tryAgain"
            >
                Try again
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>

<style scoped>
.iri-input {
    min-width: 560px;
    width: 560px;
}
</style>

