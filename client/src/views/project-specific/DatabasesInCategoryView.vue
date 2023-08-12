<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { Database } from '@/types/database';

import ResourceLoader from '@/components/ResourceLoader.vue';
import DatabaseDisplay from '@/components/database/DatabaseDisplay.vue';
import { useRouter } from 'vue-router';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';

const databasesInCategory = ref<Database[]>();
const databasesOther = ref<Database[]>();
const categoryId = useSchemaCategoryId();

async function fetchDatabases() {
    const [ resultInCategory, resultOther ] = await Promise.all([
        API.databases.getAllDatabases({}, { categoryId }),
        API.databases.getAllDatabases({}),

    ]);
    if (!resultInCategory.status || !resultOther.status)
        return false;

    databasesInCategory.value = resultInCategory.data;
    databasesOther.value = resultOther.data.filter(database => !resultInCategory.data.find(d => d.id === database.id));

    return true;
}

const router = useRouter();

function createNew() {
    router.push({ name: 'database', params: { id: 'new' }, query: { categoryId } });
}

function edit(id: Id) {
    router.push({ name: 'database', params: { id }, query: { categoryId, state: 'editing' } });
}
</script>

<template>
    <div>
        <h1>Databases in category</h1>
        <div class="databases mt-3">
            <div
                v-for="database in databasesInCategory"
                :key="database.id"
            >
                <DatabaseDisplay
                    :database="database"
                    :category-id="categoryId"
                    @edit="edit(database.id)"
                />
            </div>
        </div>
        <template v-if="databasesInCategory">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
        </template>
        <h1>Other databases</h1>
        <div class="databases mt-3">
            <div
                v-for="database in databasesOther"
                :key="database.id"
            >
                <DatabaseDisplay
                    :database="database"
                    :category-id="categoryId"
                    @edit="edit(database.id)"
                />
            </div>
        </div>
        <ResourceLoader :loading-function="fetchDatabases" />
    </div>
</template>

<style scoped>
.databases {
    display: flex;
    flex-wrap: wrap;
}
</style>
