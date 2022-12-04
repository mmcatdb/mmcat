<script lang="ts">
import { defineComponent } from 'vue';
import { Mapping, type MappingFromServer } from '@/types/mapping';
import { GET } from '@/utils/backendAPI';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        MappingDisplay
    },
    data() {
        return {
            mappings: null as Mapping[] | null,
            fetched: false
        };
    },
    async mounted() {
        await this.fetchData();
    },
    methods: {
        async fetchData() {
            //const result = await GET<MappingFromServer[]>(`/schema-categories/${getSchemaCategoryId()}/mappings`);
            const result = await GET<MappingFromServer[]>(`/logical-models/${1}/mappings`);
            if (result.status)
                this.mappings = result.data.map(Mapping.fromServer);

            this.fetched = true;
        },
        createNew() {
            this.$router.push({ name: 'accessPathEditor' });
        }
    }
});
</script>

<template>
    <div>
        <h1>Mappings</h1>
        <template v-if="mappings">
            <div class="button-row">
                <button
                    @click="createNew"
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
.mappings {
    display: flex;
    flex-wrap: wrap;
}
</style>
