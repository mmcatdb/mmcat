<script lang="ts">
import { defineComponent } from 'vue';
import { LogicalModelInfo } from '@/types/logicalModel';
import API from '@/utils/api';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import LogicalModelPreview from '@/components/LogicalModelPreview.vue';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        LogicalModelPreview
    },
    data() {
        return {
            logicalModels: null as LogicalModelInfo[] | null,
            fetched: false
        };
    },
    async mounted() {
        await this.fetchData();
    },
    methods: {
        async fetchData() {
            const result = await API.logicalModels.getAllLogicalModelInfosInCategory({ categoryId: getSchemaCategoryId() });
            if (result.status)
                this.logicalModels = result.data.map(LogicalModelInfo.fromServer);

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
                    v-for="logicalModel in logicalModels"
                    :key="logicalModel.id"
                >
                    <LogicalModelPreview :logical-model="logicalModel" />
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
