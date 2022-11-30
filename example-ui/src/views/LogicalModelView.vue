<script lang="ts">
import { defineComponent } from 'vue';
import { LogicalModel, type LogicalModelFromServer } from '@/types/logicalModel';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import LogicalModelDisplay from '@/components/LogicalModelDisplay.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        LogicalModelDisplay
    },
    data() {
        return {
            logicalModel: null as LogicalModel | null,
            fetched: false
        };
    },
    async mounted() {
        await this.fetchData();
    },
    methods: {
        async fetchData() {
            const result = await GET<LogicalModelFromServer>(`/logical-models/${this.$route.params.id}`);
            if (result.status)
                this.logicalModel = LogicalModel.fromServer(result.data);

            this.fetched = true;
        }
    }
});
</script>

<template>
    <div>
        <h1>Logical model</h1>
        <div
            v-if="logicalModel"
            class="logical-model"
        >
            <LogicalModelDisplay :logical-model="logicalModel" />
        </div>
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.logical-model {
    display: flex;
}
</style>
