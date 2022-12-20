<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { LogicalModelInfo } from '@/types/logicalModel';
import API from '@/utils/api';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import LogicalModelDisplay from '@/components/LogicalModelDisplay.vue';
import { DatabaseInfo } from '@/types/database';
import { useRouter } from 'vue-router';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';

type LogicalModelDatabase = {
    logicalModel: LogicalModelInfo;
    database: DatabaseInfo;
};

const infos = ref<LogicalModelDatabase[]>();
const fetched = ref(false);

onMounted(fetchData);

const schemaCategoryId = useSchemaCategory();

async function fetchData() {
    const result = await API.logicalModels.getAllLogicalModelDatabaseInfosInCategory({ categoryId: schemaCategoryId });
    if (result.status) {
        infos.value = result.data.map(info => ({
            logicalModel: LogicalModelInfo.fromServer(info.logicalModel),
            database: DatabaseInfo.fromServer(info.database)
        }));
    }

    fetched.value = true;
}

const router = useRouter();
function createNew() {
    router.push({ name: 'newLogicalModel' });
}
</script>

<template>
    <div>
        <h1>Logical models</h1>
        <template v-if="infos">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
            <div class="logical-models">
                <div
                    v-for="info in infos"
                    :key="info.logicalModel.id"
                >
                    <LogicalModelDisplay
                        :logical-model="info.logicalModel"
                        :database="info.database"
                    />
                </div>
            </div>
        </template>
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.logical-models {
    display: flex;
    flex-wrap: wrap;
}
</style>
