<script lang="ts">
import { defineComponent } from 'vue';
import { Model } from '@/types/model';
import API from '@/utils/api';

import ModelDisplay from '@/components/ModelDisplay.vue';
import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';

export default defineComponent({
    components: {
        ModelDisplay,
        ResourceNotFound,
        ResourceLoading
    },
    data() {
        return {
            model: null as Model | null,
            fetched: false
        };
    },
    async mounted() {
        await this.fetchData();
    },
    methods: {
        async fetchData() {
            const result = await API.models.getModel({ jobId: this.$route.params.jobId });
            if (result.status)
                this.model = Model.fromServer(result.data);

            this.fetched = true;
        }
    }
});
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
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.model {
    display: flex;
}
</style>
