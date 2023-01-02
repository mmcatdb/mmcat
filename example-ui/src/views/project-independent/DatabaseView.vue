<script setup lang="ts">
import API from '@/utils/api';
import type { Database } from '@/types/database';

import ResourceLoader from '@/components/ResourceLoader.vue';
import DatabaseDisplay from '@/components/database/DatabaseDisplay.vue';
import DatabaseEditor from '@/components/database/DatabaseEditor.vue';
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const rawId = route.params.id as string;
const isNew = rawId === 'new';
const id = isNew ? null : parseInt(rawId);

const isEditing = ref(isNew || route.params.state === 'editing');
const database = ref<Database>();

const shouldReturnToAllDatabasesAfterEditing = isEditing.value;

async function fetchDatabase() {
    if (isNew || !id)
        return true;

    const result = await API.databases.getDatabase({ id: id });
    if (!result.status)
        return false;

    database.value = result.data;
    return true;
}

function save(newValue: Database) {
    if (shouldReturnToAllDatabasesAfterEditing) {
        router.push({ name: 'databases' });
        return;
    }

    database.value = newValue;
    isEditing.value = false;
}

function cancel() {
    if (shouldReturnToAllDatabasesAfterEditing) {
        router.push({ name: 'databases' });
        return;
    }

    isEditing.value = false;
}

function deleteFunction() {
    router.push({ name: 'databases' });
}
</script>

<template>
    <div>
        <template v-if="isNew">
            <h1>Create new database</h1>
            <div class="database">
                <DatabaseEditor
                    @save="save"
                    @cancel="cancel"
                />
            </div>
        </template>
        <template v-else>
            <h1>Database</h1>
            <div
                v-if="database"
                class="database"
            >
                <DatabaseEditor
                    v-if="isEditing"
                    :database="database"
                    @save="save"
                    @cancel="cancel"
                    @delete="deleteFunction"
                />
                <DatabaseDisplay
                    v-else
                    :database="database"
                    @edit="isEditing = true"
                />
            </div>
            <ResourceLoader :loading-function="fetchDatabase" />
        </template>
    </div>
</template>

<style scoped>
.database {
    display: flex;
}
</style>
