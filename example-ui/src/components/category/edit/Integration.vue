<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
import dataspecerAPI from '@/utils/api/dataspecerAPI';
import { defineComponent } from 'vue';
import { addImportedToGraph, importDataspecer, type ImportedDataspecer } from '@/utils/integration';

//const EXAMPLE_UUID = '537f6a7e-0883-4d57-a19c-f275bd28af9f';
const EXAMPLE_UUID = 'f2480523-c3ee-4c3c-a36a-9483749bc0d6';

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
            uuid: EXAMPLE_UUID,
            EXAMPLE_UUID,
            imported: undefined as ImportedDataspecer | undefined
        };
    },
    methods: {
        async importPIM() {
            const result = await dataspecerAPI.GET<unknown>(`/store/${this.uuid}`);
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
                        UUID:
                    </td>
                    <td class="value">
                        <input
                            v-model="uuid"
                            class="uuid-input"
                        />
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        Example:
                    </td>
                    <td class="value">
                        {{ EXAMPLE_UUID }}
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
                :disabled="!uuid"
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

.uuid-input {
    min-width: 260px;
    width: 260px;
}
</style>

