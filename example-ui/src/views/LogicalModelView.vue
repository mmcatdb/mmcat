<script lang="ts">
import { defineComponent } from 'vue';
import { LogicalModel, type LogicalModelFromServer } from '@/types/logicalModel';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import LogicalModelDisplay from '@/components/LogicalModelDisplay.vue';
import type { Mapping } from '@/types/mapping';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        LogicalModelDisplay
    },
    data() {
        return {
            fetched: false,
            logicalModel: null as LogicalModel | null,
            mappings: [] as Mapping[] // TODO TODO TODO FIXXXXXXXXXXX
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
        <template v-if="logicalModel">
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
