<script setup lang="ts">
import { ref } from 'vue';
import { LogicalModelInfo } from '@/types/logicalModel';
import API from '@/utils/api';

import ResourceLoader from '@/components/ResourceLoader.vue';
import LogicalModelDisplay from '@/components/LogicalModelDisplay.vue';
import { DatabaseInfo } from '@/types/database';
import { useRouter } from 'vue-router';
import { useSchemaCategoryId } from '@/utils/injects';

type LogicalModelDatabase = {
    logicalModel: LogicalModelInfo;
    database: DatabaseInfo;
};

const infos = ref<LogicalModelDatabase[]>();

const categoryId = useSchemaCategoryId();

async function fetchModels() {
    const result = await API.logicalModels.getAllLogicalModelDatabaseInfosInCategory({ categoryId });
    if (!result.status)
        return false;

    infos.value = result.data.map(info => ({
        logicalModel: LogicalModelInfo.fromServer(info.logicalModel),
        database: DatabaseInfo.fromServer(info.database),
    }));

    return true;
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
        <ResourceLoader :loading-function="fetchModels" />
    </div>
</template>

<style scoped>
.logical-models {
    display: flex;
    flex-wrap: wrap;
}
</style>
