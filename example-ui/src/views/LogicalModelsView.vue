<script lang="ts">
import { defineComponent } from 'vue';
import { LogicalModelInfo } from '@/types/logicalModel';
import API from '@/utils/api';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import LogicalModelDisplay from '@/components/LogicalModelDisplay.vue';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';
import { DatabaseInfo } from '@/types/database';

type LogicalModelDatabase = {
    logicalModel: LogicalModelInfo;
    database: DatabaseInfo;
};

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        LogicalModelDisplay
    },
    data() {
        return {
            infos: null as LogicalModelDatabase[] | null,
            fetched: false
        };
    },
    async mounted() {
        await this.fetchData();
    },
    methods: {
        async fetchData() {
            const result = await API.logicalModels.getAllLogicalModelDatabaseInfosInCategory({ categoryId: getSchemaCategoryId() });
            if (result.status) {
                this.infos = result.data.map(info => ({
                    logicalModel: LogicalModelInfo.fromServer(info.logicalModel),
                    database: DatabaseInfo.fromServer(info.database)
                }));
            }

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
        <template v-if="infos">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
            <div class="logical-models">
                <div
                    v-for="info in infos"
                    :key="info.logicalModel.id"
                >
                    <LogicalModelDisplay
                        :logical-model="info.logicalModel"
                        :database="info.database"
                    />
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
