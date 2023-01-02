<script lang="ts">
import { defineComponent } from 'vue';
import { Model } from '@/types/model';
import API from '@/utils/api';

import ModelDisplay from '@/components/ModelDisplay.vue';
import ResourceLoader from '@/components/ResourceLoader.vue';

export default defineComponent({
    components: {
        ModelDisplay,
        ResourceLoader
    },
    data() {
        return {
            model: null as Model | null
        };
    },
    methods: {
        async fetchModel() {
            const result = await API.models.getModel({ jobId: this.$route.params.jobId });
            if (!result.status)
                return false;

            this.model = Model.fromServer(result.data);
            return true;
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
        <ResourceLoader :loading-function="fetchModel" />
    </div>
</template>

<style scoped>
.model {
    display: flex;
}
</style>
