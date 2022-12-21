<script setup lang="ts">
import { onMounted, ref } from 'vue';
import API from '@/utils/api';
import type { Database } from '@/types/database/Database';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import DatabaseDisplay from '@/components/database/DatabaseDisplay.vue';
import { useRouter } from 'vue-router';
import { tryUseSchemaCategory } from '@/utils/globalSchemaSettings';

const databases = ref<Database[]>();
const fetched = ref(false);

onMounted(() => {
    fetchData();
});

async function fetchData() {
    const categoryId = tryUseSchemaCategory();
    const queryParams = categoryId !== undefined ? { categoryId } : undefined;
    const result = await API.databases.getAllDatabases({}, queryParams);
    if (result.status)
        databases.value = result.data;

    fetched.value = true;
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
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.databases {
    display: flex;
    flex-wrap: wrap;
}
</style>
