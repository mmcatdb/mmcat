<script lang="ts">
import { defineComponent } from 'vue';
import { ModelView, type ModelViewFromServer } from '@/types/model';
import { GET } from '@/utils/backendAPI';

import ModelViewDisplay from '@/components/ModelViewDisplay.vue';
import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';

export default defineComponent({
    components: {
        ModelViewDisplay,
        ResourceNotFound,
        ResourceLoading
    },
    data() {
        return {
            models: null as ModelView[] | null,
            fetched: false
        };
    },
    async mounted() {
        await this.fetchData();
    },
    methods: {
        async fetchData() {
            const result = await GET<ModelViewFromServer[]>(`/schema-categories/${getSchemaCategoryId()}/models`);
            if (result.status)
                this.models = result.data.map(ModelView.fromServer);

            this.fetched = true;
        }
    }
});
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
