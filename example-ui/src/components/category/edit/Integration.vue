<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
import dataspecerAPI from '@/utils/api/dataspecerAPI';
import { defineComponent } from 'vue';
import { addImportedToGraph, importDataspecer, type ImportedDataspecer } from '@/utils/integration';

//const EXAMPLE_IRI = '537f6a7e-0883-4d57-a19c-f275bd28af9f';
//const EXAMPLE_IRI = 'f2480523-c3ee-4c3c-a36a-9483749bc0d6';
const EXAMPLE_IRI = 'https://ofn.gov.cz/data-specification/c697d1a1-4df1-4292-a136-df593fb6a32b';

export default defineComponent({
    components: {

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
            imported: undefined as ImportedDataspecer | undefined
        };
    },
    methods: {
        async importPIM() {
            const result = await dataspecerAPI.getStoreForIri(this.iri);
            if (result.status) {
                console.log(result.data);
                const imported = importDataspecer(result.data);
                console.log(imported);
                this.imported = imported;
            }
        },
        save() {
            if (!this.imported)
                return;

            addImportedToGraph(this.imported, this.graph);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        }
    }
});
</script>

<template>
    <div>
        <h2>Integration</h2>
        <template v-if="!imported">
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
            <p>
                Import was successful! Do you want to create the objects and morphisms?
            </p>
            <table>
                <tr>
                    <td class="label">
                        Objects:
                    </td>
                    <td class="value">
                        {{ imported.objects.length }}
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        Morphisms:
                    </td>
                    <td class="value">
                        {{ imported.morphisms.length }}
                    </td>
                </tr>
            </table>
        </template>
        <div class="button-row">
            <button
                v-if="!imported"
                :disabled="!iri"
                @click="importPIM"
            >
                Import
            </button>
            <button
                v-if="imported"
                @click="save"
            >
                Create all
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
.value {
    font-weight: bold;
}

.iri-input {
    min-width: 560px;
    width: 560px;
}
</style>

