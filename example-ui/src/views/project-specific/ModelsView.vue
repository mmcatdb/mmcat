<script setup lang="ts">
import { ref } from 'vue';
import { ModelView } from '@/types/model';
import API from '@/utils/api';

import { useSchemaCategoryId } from '@/utils/injects';
import ModelViewDisplay from '@/components/ModelViewDisplay.vue';
import ResourceLoader from '@/components/ResourceLoader.vue';

const models = ref<ModelView[]>();

const categoryId = useSchemaCategoryId();

async function fetchModels() {
    const result = await API.models.getAllModelsInCategory({ categoryId });
    if (!result.status)
        return false;

    models.value = result.data.map(ModelView.fromServer);
    return true;
}
</script>

<template>
    <div>
        <h1>Models</h1>
        <div
            v-if="models"
            class="models"
        >
            <ModelViewDisplay
                v-for="model in models"
                :key="model.jobId"
                :model="model"
            />
        </div>
        <ResourceLoader :loading-function="fetchModels" />
    </div>
</template>

<style scoped>
.models {
    display: flex;
}
</style>
