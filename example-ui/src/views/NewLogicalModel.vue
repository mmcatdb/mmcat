<script lang="ts">
import { DatabaseWithConfiguration } from '@/types/database';
import API from '@/utils/api';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';
import { defineComponent } from 'vue';

export default defineComponent({
    components: {

    },
    data() {
        return {
            databases: [] as DatabaseWithConfiguration[],
            selectedDatabase: null as DatabaseWithConfiguration | null,
            label: '',
            fetching: false
        };
    },
    async mounted() {
        const result = await API.databases.getAllDatabaseInfos({});
        if (result.status)
            this.databases = result.data.map(DatabaseWithConfiguration.fromServer);
    },
    methods: {
        async createLogicalModel() {
            if (!this.selectedDatabase || !this.label)
                return;

            this.fetching = true;

            const result = await API.logicalModels.createNewLogicalModel({}, {
                databaseId: this.selectedDatabase.id,
                categoryId: getSchemaCategoryId(),
                jsonValue: JSON.stringify({
                    label: this.label
                })
            });
            if (result.status)
                this.$router.push({ name: 'logicalModels' });

            this.fetching = false;
        }
    }
});
</script>

<template>
    <div>
        <h1>Create a new logical model</h1>
        <table>
            <tr>
                <td class="label">
                    Database:
                </td>
                <td class="value">
                    <select v-model="selectedDatabase">
                        <option
                            v-for="database in databases"
                            :key="database.id"
                            :value="database"
                        >
                            {{ database.label }}
                        </option>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="label">
                    Label:
                </td>
                <td class="value">
                    <input v-model="label" />
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                :disabled="fetching || !selectedDatabase || !label"
                @click="createLogicalModel"
            >
                Create logical model
            </button>
        </div>
    </div>
</template>
