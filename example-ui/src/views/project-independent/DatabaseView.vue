<script setup lang="ts">
import API from '@/utils/api';
import type { Database } from '@/types/database';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import DatabaseDisplay from '@/components/database/DatabaseDisplay.vue';
import DatabaseEditor from '@/components/database/DatabaseEditor.vue';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const rawId = route.params.id as string;
const isNew = rawId === 'new';
const id = isNew ? null : parseInt(rawId);

const isEditing = ref(isNew || route.params.state === 'editing');
const database = ref<Database>();
const fetched = ref(false);

const shouldReturnToAllDatabasesAfterEditing = isEditing.value;

onMounted(async () => {
    if (!isNew)
        fetchData();
});

async function fetchData() {
    if (!id)
        return;

    const result = await API.databases.getDatabase({ id: id });
    if (result.status)
        database.value = result.data;

    fetched.value = true;
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
            <ResourceNotFound v-else-if="fetched" />
            <ResourceLoading v-else />
        </template>
    </div>
</template>

<style scoped>
.database {
    display: flex;
}
</style>
