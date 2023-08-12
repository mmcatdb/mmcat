<script setup lang="ts">
import { ref } from 'vue';
import { Model } from '@/types/model';
import API from '@/utils/api';

import ModelDisplay from '@/components/ModelDisplay.vue';
import ResourceLoader from '@/components/ResourceLoader.vue';
import { useRoute } from 'vue-router';

const model = ref<Model>();

const route = useRoute();

async function fetchModel() {
    const result = await API.models.getModel({ jobId: route.params.jobId });
    if (!result.status)
        return false;

    model.value = Model.fromServer(result.data);
    return true;
}
</script>

<template>
    <div>
        <h1>Model</h1>
        <div
            v-if="model"
            class="model"
        >
            <ModelDisplay
                :model="model"
            />
        </div>
        <ResourceLoader :loading-function="fetchModel" />
    </div>
</template>

<style scoped>
.model {
    display: flex;
}
</style>
