<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ModelView } from '@/types/model';
import API from '@/utils/api';

import ModelViewDisplay from '@/components/ModelViewDisplay.vue';
import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';

const models = ref<ModelView[]>();
const fetched = ref(false);

onMounted(fetchData);

const schemaCategoryId = useSchemaCategory();

async function fetchData() {
    const result = await API.models.getAllModelsInCategory({ categoryId: schemaCategoryId });
    if (result.status)
        models.value = result.data.map(ModelView.fromServer);

    fetched.value = true;
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
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.models {
    display: flex;
}
</style>
