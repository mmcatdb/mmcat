<script lang="ts">
import { defineComponent } from 'vue';
import { LogicalModel, type LogicalModelFromServer } from '@/types/logicalModel';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import LogicalModelDisplay from '@/components/LogicalModelDisplay.vue';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        LogicalModelDisplay
    },
    data() {
        return {
            logicalModels: null as LogicalModel[] | null,
            fetched: false
        };
    },
    async mounted() {
        await this.fetchData();
    },
    methods: {
        async fetchData() {
            const result = await GET<LogicalModelFromServer[]>(`/schema-categories/${getSchemaCategoryId()}/logical-models`);
            if (result.status)
                this.logicalModels = result.data.map(LogicalModel.fromServer);

            this.fetched = true;
        },
        createNew() {
            this.$router.push({ name: 'newLogicalModel' });
        }
    }
});
</script>

<template>
    <div>
        <h1>Logical models</h1>
        <template v-if="logicalModels">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
            <div class="logical-models">
                <div
                    v-for="(logicalModel, index) in logicalModels"
                    :key="index"
                >
                    <LogicalModelDisplay :logical-model="logicalModel" />
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
