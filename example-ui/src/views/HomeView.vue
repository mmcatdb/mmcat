<script lang="ts">
import { defineComponent } from 'vue';
import { getSchemaCategoryId, setSchemaCategoryId } from '@/utils/globalSchemaSettings';
import { SchemaCategoryInfo, type SchemaCategoryInfoFromServer } from '@/types/schema';
import { GET, POST } from '@/utils/backendAPI';
const DOCUMENTATION_URL = import.meta.env.VITE_DOCUMENTATION_URL;

export default defineComponent({
    props: {

    },
    data() {
        return {
            selectedSchema: undefined as SchemaCategoryInfo | undefined,
            currentSchema: undefined as SchemaCategoryInfo | undefined,
            avaliableSchemas: [] as SchemaCategoryInfo[],
            newSchemaName: '',
            DOCUMENTATION_URL
        };
    },
    async mounted() {
        const result = await GET<SchemaCategoryInfoFromServer[]>('/schemaCategories');
        if (!result.status)
            return;

        this.avaliableSchemas = result.data.map(infoFromServer => SchemaCategoryInfo.fromServer(infoFromServer));
        const currentId = getSchemaCategoryId();
        this.currentSchema = this.avaliableSchemas.find(schema => schema.id === currentId);
    },
    methods: {
        confirmNewId() {
            if (!this.selectedSchema)
                return;

            this.currentSchema = this.selectedSchema;
            setSchemaCategoryId(this.selectedSchema.id);
        },
        async confirmNewSchema() {
            const jsonValue = JSON.stringify({ name: this.newSchemaName });
            const result = await POST<SchemaCategoryInfoFromServer>('/schemaCategories', { jsonValue });
            if (!result.status)
                return;

            const newSchema = SchemaCategoryInfo.fromServer(result.data);
            this.avaliableSchemas.push(newSchema);

            this.newSchemaName = '';
        }
    }
});
</script>

<template>
    <h1>MM-cat</h1>
    <p>
        A multi-model data modelling framework based on category theory.
    </p>
    <br />
    <p>
        Detailed instructions on how to use this tool can be found <a :href="DOCUMENTATION_URL">here</a>.
    </p>
    <div class="divide">
        <div class="editor">
            <h2>Select schema category</h2>
            <table>
                <tr>
                    <td class="label">
                        Current schema:
                    </td>
                    <td class="value">
                        {{ currentSchema?.name }}
                    </td>
                </tr>
                <tr>
                    <td class="label">
                        New value:
                    </td>
                    <td class="value">
                        <select v-model="selectedSchema">
                            <option
                                v-for="schema in avaliableSchemas"
                                :key="schema.id"
                                :value="schema"
                            >
                                {{ schema.name }}
                            </option>
                        </select>
                    </td>
                </tr>
            </table>
            <div class="button-row">
                <button
                    :disabled="!selectedSchema || selectedSchema?.id === currentSchema?.id"
                    @click="confirmNewId"
                >
                    Confirm
                </button>
            </div>
        </div>
        <div class="editor">
            <h2>Add schema category</h2>
            <table>
                <tr>
                    <td class="label">
                        Name:
                    </td>
                    <td class="value">
                        <input v-model="newSchemaName" />
                    </td>
                </tr>
                <tr>&nbsp;</tr>
            </table>
            <div class="button-row">
                <button
                    :disabled="!newSchemaName"
                    @click="confirmNewSchema"
                >
                    Confirm
                </button>
            </div>
        </div>
    </div>
</template>

<style>
.divide {
    margin-top: 24px;
}

.editor {
    display: flex;
    flex-direction: column;
}
</style>
