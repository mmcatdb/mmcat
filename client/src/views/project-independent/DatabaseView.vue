<script setup lang="ts">
import API from '@/utils/api';
import type { Database } from '@/types/database';

import ResourceLoader from '@/components/common/ResourceLoader.vue';
import DatabaseDisplay from '@/components/database/DatabaseDisplay.vue';
import DatabaseEditor from '@/components/database/DatabaseEditor.vue';
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toQueryScalar } from '@/utils/router';

const route = useRoute();
const router = useRouter();

const rawId = route.params.id as string;
const isNew = rawId === 'new';
const id = isNew ? null : parseInt(rawId);

const isEditing = ref(isNew || route.query.state === 'editing');
const categoryId = toQueryScalar(route.query.categoryId);
const returnPath = categoryId
    ? { name: 'databasesInCategory', params: { categoryId } }
    : { name: 'databases' };

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
        router.push(returnPath);
        return;
    }

    database.value = newValue;
    isEditing.value = false;
}

function cancel() {
    if (shouldReturnToAllDatabasesAfterEditing) {
        router.push(returnPath);
        return;
    }

    isEditing.value = false;
}
</script>

<template>
    <div>
        <template v-if="isNew">
            <h1>Create new database</h1>
            <div class="database mt-3">
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
                class="database mt-3"
            >
                <DatabaseEditor
                    v-if="isEditing"
                    :database="database"
                    @save="save"
                    @cancel="cancel"
                    @delete="router.push(returnPath)"
                />
                <DatabaseDisplay
                    v-else
                    :database="database"
                    @edit="isEditing = true"
                />
            </div>
            <ResourceLoader :loading-function="fetchDatabase" />
            <div class="button-row">
                <button
                    v-if="!isEditing"
                    @click="router.push(returnPath)"
                >
                    Back
                </button>
            </div>
        </template>
    </div>
</template>

<style scoped>
.database {
    display: flex;
}
</style>
