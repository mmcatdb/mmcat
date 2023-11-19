<script setup lang="ts">
import { shallowRef } from 'vue';
import { useSchemaCategoryId } from '@/utils/injects';
import { queryWithVersionFromServer, type QueryWithVersion, QueryVersion } from '@/types/query';
import API from '@/utils/api';
import QueryDisplay from '@/components/query/QueryDisplay.vue';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import type { Evocat } from '@/types/evocat/Evocat';
import type { Graph } from '@/types/categoryGraph';
import EvocatDisplay from '@/components/category/EvocatDisplay.vue';

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

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();

function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    graph.value = context.graph;
}
</script>

<template>
    <div>
        <h1 class="mb-3">
            Queries
        </h1>
        <template v-if="queries">
            <div class="row">
                <div class="col-6 d-flex flex-column gap-3">
                    <QueryDisplay
                        v-for="query in queries"
                        :key="query.query.id"
                        :version="query.version"
                        @create-query-version="updateQuery"
                    />
                </div>
                <div class="col-6">
                    <div class="responsive-evocat-display">
                        <EvocatDisplay @evocat-created="evocatCreated" />
                    </div>
                </div>
            </div>
        </template>
        <ResourceLoader :loading-function="fetchQueries" />
    </div>
</template>

<style scoped>
.responsive-evocat-display {
    --schema-category-canvas-width: 100%;
    position: sticky;
    top: 74px;
}
</style>
