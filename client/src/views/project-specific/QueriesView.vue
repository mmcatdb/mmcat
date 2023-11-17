<script setup lang="ts">
import { shallowRef } from 'vue';
import { useSchemaCategoryId } from '@/utils/injects';
import { queryWithVersionFromServer, type QueryWithVersion, QueryVersion } from '@/types/query';
import API from '@/utils/api';
import QueryDisplay from '@/components/query/QueryDisplay.vue';
import ResourceLoader from '@/components/ResourceLoader.vue';

const categoryId = useSchemaCategoryId();

const queries = shallowRef<QueryWithVersion[]>();

async function fetchQueries() {
    const result = await API.queries.getQueriesInCategory({ categoryId });
    if (!result.status)
        return false;

    queries.value = result.data.map(queryWithVersionFromServer);
    return true;
}

function updateQuery(newVersion: QueryVersion) {
    if (!queries.value)
        return;

    const newQueries = [ ...queries.value ];
    const index = newQueries.findIndex(q => q.query.id === newVersion.query.id);
    if (index === -1)
        return;

    newQueries[index] = { query: newVersion.query, version: newVersion };
    queries.value = newQueries;
}
</script>

<template>
    <div>
        <h1>Queries</h1>
        <template v-if="queries">
            <QueryDisplay
                v-for="query in queries"
                :key="query.query.id"
                :version="query.version"
                @create-query-version="updateQuery"
            />
        </template>
        <ResourceLoader :loading-function="fetchQueries" />
    </div>
</template>

<style scoped>
.query-input, .query-output {
    min-width: 600px;
}
</style>
