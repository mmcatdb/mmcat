<script lang="ts">
import { defineComponent } from 'vue';
import { GET } from '@/utils/backendAPI';
import type { Database } from '@/types/database/Database';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import DatabaseDisplay from '@/components/database/DatabaseDisplay.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        DatabaseDisplay
    },
    data() {
        return {
            databases: null as Database[] | null,
            fetched: false,
        };
    },
    mounted() {
        this.fetchData();
    },
    methods: {
        async fetchData() {
            const result = await GET<Database[]>('/databases');
            if (result.status)
                this.databases = result.data;

            this.fetched = true;
        },
        createNew() {
            this.$router.push({ name: 'database', params: { id: 'new' } });
        }
    }
});
</script>

<template>
    <div>
        <h1>Databases</h1>
        <template v-if="databases">
            <div class="button-row">
                <button
                    @click="createNew"
                >
                    Create new
                </button>
            </div>
            <div
                class="databases"
            >
                <div
                    v-for="(database, index) in databases"
                    :key="index"
                >
                    <DatabaseDisplay
                        :database="database"
                        @edit="$router.push({ name: 'database', params: { id: database.id, state: 'editing' } });"
                    />
                </div>
            </div>
        </template>
        <ResourceNotFound v-else-if="fetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
.databases {
    display: flex;
    flex-wrap: wrap;
}
</style>
