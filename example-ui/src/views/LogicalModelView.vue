<script lang="ts">
import { defineComponent } from 'vue';
import { LogicalModel } from '@/types/logicalModel';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import LogicalModelDisplay from '@/components/LogicalModelDisplay.vue';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';
import { Mapping } from '@/types/mapping';
import API from '@/utils/api';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        LogicalModelDisplay,
        MappingDisplay
    },
    data() {
        return {
            fetched: false,
            logicalModel: null as LogicalModel | null,
            mappings: null as Mapping[] | null
        };
    },
    async mounted() {
        await this.fetchData();
    },
    methods: {
        async fetchData() {
            const result = await API.logicalModels.getLogicalModel({ id: this.$route.params.id });
            if (!result.status) {
                this.fetched = true;
                return;
            }

            this.logicalModel = LogicalModel.fromServer(result.data);

            const mappingsResult = await API.mappings.getAllMappingsInLogicalModel({ logicalModelId: this.logicalModel.id });
            if (mappingsResult.status) {
                this.mappings = mappingsResult.data.map(Mapping.fromServer);
                return;
            }

            this.fetched = true;
        },
        createNewMapping() {
            this.$router.push({ name: 'accessPathEditor' });
        }
    }
});
</script>

<template>
    <div>
        <h1>Logical model</h1>
        <template v-if="logicalModel && mappings">
            <div class="logical-model">
                <LogicalModelDisplay :logical-model="logicalModel" />
            </div>
            <h2>Mappings</h2>
            <div class="button-row">
                <button
                    @click="createNewMapping"
                >
                    Create new
                </button>
            </div>
            <div class="mappings">
                <div
                    v-for="mapping in mappings"
                    :key="mapping.id"
                >
                    <MappingDisplay :mapping="mapping" />
                </div>
            </div>
        </template>
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.logical-model {
    display: flex;
}

.mappings {
    display: flex;
    flex-wrap: wrap;
}
</style>
