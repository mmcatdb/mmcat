<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { Database } from '@/types/database';

import ResourceLoader from '@/components/ResourceLoader.vue';
import DatabaseDisplay from '@/components/database/DatabaseDisplay.vue';
import { useRouter } from 'vue-router';
import { tryUseSchemaCategory } from '@/utils/globalSchemaSettings';

const databases = ref<Database[]>();

async function fetchDatabases() {
    const categoryId = tryUseSchemaCategory();
    const queryParams = categoryId !== undefined ? { categoryId } : undefined;
    const result = await API.databases.getAllDatabases({}, queryParams);
    if (!result.status)
        return false;

    databases.value = result.data;
    return true;
}

const router = useRouter();

function createNew() {
    router.push({ name: 'database', params: { id: 'new' } });
}
</script>

<template>
    <div>
        <h1>Databases</h1>
        <template v-if="databases">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
            <div
                class="databases"
            >
                <div
                    v-for="database in databases"
                    :key="database.id"
                >
                    <DatabaseDisplay
                        :database="database"
                        @edit="$router.push({ name: 'database', params: { id: database.id, state: 'editing' } });"
                    />
                </div>
            </div>
        </template>
        <ResourceLoader :loading-function="fetchDatabases" />
    </div>
</template>

<style scoped>
.databases {
    display: flex;
    flex-wrap: wrap;
}
</style>
