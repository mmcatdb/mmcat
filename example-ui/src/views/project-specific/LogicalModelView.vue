<script setup lang="ts">
import { ref } from 'vue';
import { LogicalModel } from '@/types/logicalModel';

import ResourceLoader from '@/components/ResourceLoader.vue';
import LogicalModelDisplay from '@/components/LogicalModelDisplay.vue';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';
import API from '@/utils/api';
import { useRoute, useRouter } from 'vue-router';

const logicalModel = ref<LogicalModel>();

const route = useRoute();

async function fetchModel() {
    const result = await API.logicalModels.getLogicalModel({ id: route.params.id });
    if (!result.status)
        return false;

    logicalModel.value = LogicalModel.fromServer(result.data);
    return true;
}

const router = useRouter();

function createNewMapping() {
    router.push({ name: 'accessPathEditor', query: { logicalModelId: route.params.id } });
}
</script>

<template>
    <div>
        <h1>Logical model</h1>
        <template v-if="logicalModel">
            <div class="logical-model">
                <LogicalModelDisplay
                    :logical-model="logicalModel"
                    :database="logicalModel.database"
                />
            </div>
            <h2>Mappings</h2>
            <div class="button-row">
                <button
                    @click="createNewMapping"
                >
                    Create new
                </button>
            </div>
            <div class="mappings">
                <div
                    v-for="mapping in logicalModel.mappings"
                    :key="mapping.id"
                >
                    <MappingDisplay :mapping="mapping" />
                </div>
            </div>
        </template>
        <ResourceLoader :loading-function="fetchModel" />
    </div>
</template>

<style scoped>
.logical-model {
    display: flex;
}

.mappings {
    display: flex;
    flex-wrap: wrap;
}
</style>
