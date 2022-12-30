<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
import dataspecerAPI from '@/utils/api/dataspecerAPI';
import { defineComponent } from 'vue';
import { addImportedToGraph, importDataspecer } from '@/utils/integration';
import type { ImportedDataspecer } from '@/types/integration';
import Divider from '@/components/layout/Divider.vue';

//const EXAMPLE_IRI = '537f6a7e-0883-4d57-a19c-f275bd28af9f';
//const EXAMPLE_IRI = 'f2480523-c3ee-4c3c-a36a-9483749bc0d6';
//const EXAMPLE_IRI = 'https://ofn.gov.cz/data-specification/c697d1a1-4df1-4292-a136-df593fb6a32b';
const EXAMPLE_IRI = 'https://ofn.gov.cz/data-specification/968c3ca7-bc39-4ecc-92c3-ad105cb01a5e';

export default defineComponent({
    components: {
        Divider
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            iri: EXAMPLE_IRI,
            EXAMPLE_IRI,
            importedDataspecer: undefined as ImportedDataspecer | undefined,
            fetched: false,
            error: ''
        };
    },
    methods: {
        async importPIM() {
            const result = await dataspecerAPI.getStoreForIri(this.iri);
            if (result.status) {
                console.log(result.data);
                const imported = importDataspecer(result.data);
                console.log(imported);
                this.importedDataspecer = imported;
            }
            else {
                this.error = result.error;
            }

            this.fetched = true;
        },
        save() {
            if (!this.importedDataspecer)
                return;

            addImportedToGraph(this.importedDataspecer, this.graph);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        tryAgain() {
            this.fetched = false;
        }
    }
});
</script>

<template>
    <div>
        <h2>Integration</h2>
        <template v-if="!fetched">
            <p>
                Import project from Dataspecer!
            </p>
            <table>
                <tr>
                    <td class="label">
                        IRI:
                    </td>
                    <td class="value">
                        <input
                            v-model="iri"
                            class="iri-input"
                        />
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        Example:
                    </td>
                    <td class="value">
                        {{ EXAMPLE_IRI }}
                    </td>
                </tr>
            </table>
        </template>
        <template v-else>
            <template v-if="importedDataspecer">
                <p>
                    Import successful! The following resources were found:
                </p>
                <table>
                    <tr>
                        <td class="label">
                            Classes:
                        </td>
                        <td class="value">
                            {{ importedDataspecer.counts.classes }}
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            Attributes:
                        </td>
                        <td class="value">
                            {{ importedDataspecer.counts.attributes }}
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            Associations:
                        </td>
                        <td class="value">
                            {{ importedDataspecer.counts.associations }}
                        </td>
                    </tr>
                </table>
                <Divider />
                <p>
                    Do you want to create the objects and morphisms?
                </p>
                <table>
                    <tr>
                        <td class="label">
                            Objects:
                        </td>
                        <td class="value">
                            {{ importedDataspecer.objects.length }}
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            Morphisms:
                        </td>
                        <td class="value">
                            {{ importedDataspecer.morphisms.length }}
                        </td>
                    </tr>
                </table>
            </template>
            <template v-else>
                {{ error }}
            </template>
        </template>
        <div class="button-row">
            <button
                v-if="!importedDataspecer && !fetched"
                :disabled="!iri"
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
                v-if="fetched && !importedDataspecer"
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

