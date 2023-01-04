<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { DatabaseWithConfiguration } from '@/types/database';
import API from '@/utils/api';
import { useRouter } from 'vue-router';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

const databases = ref<DatabaseWithConfiguration[]>();
const selectedDatabase = ref<DatabaseWithConfiguration>();
const label = ref('');
const fetching = ref(false);

onMounted(async () => {
    const result = await API.databases.getAllDatabaseInfos({});
    if (result.status)
        databases.value = result.data.map(DatabaseWithConfiguration.fromServer);
});

const router = useRouter();

const schemaCategoryId = useSchemaCategory();

async function createLogicalModel() {
    if (!selectedDatabase.value || !label.value)
        return;

    fetching.value = true;

    const result = await API.logicalModels.createNewLogicalModel({}, {
        databaseId: selectedDatabase.value.id,
        categoryId: schemaCategoryId,
        jsonValue: JSON.stringify({
            label: label.value
        })
    });
    if (result.status)
        router.push({ name: 'logicalModels' });

    fetching.value = false;
}
</script>

<template>
    <div>
        <h1>Create a new logical model</h1>
        <ValueContainer>
            <ValueRow label="Database:">
                <select v-model="selectedDatabase">
                    <option
                        v-for="database in databases"
                        :key="database.id"
                        :value="database"
                    >
                        {{ database.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow label="Label:">
                <input v-model="label" />
            </ValueRow>
        </ValueContainer>
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
