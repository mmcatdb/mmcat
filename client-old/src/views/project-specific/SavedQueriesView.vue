<script setup lang="ts">
import { shallowRef } from 'vue';
import { useSchemaCategoryId } from '@/utils/injects';
import { Query } from '@/types/query';
import API from '@/utils/api';
import QueryDisplay from '@/components/query/QueryDisplay.vue';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import type { Evocat } from '@/types/evocat/Evocat';
import type { Graph } from '@/types/categoryGraph';
import EvocatDisplay from '@/components/category/EvocatDisplay.vue';

const categoryId = useSchemaCategoryId();

const queries = shallowRef<Query[]>();

async function fetchQueries() {
    const result = await API.queries.getQueriesInCategory({ categoryId });
    if (!result.status)
        return false;

    queries.value = result.data.map(Query.fromServer);
    return true;
}

function updateQuery(newQuery: Query) {
    if (!queries.value)
        return;

    const newQueries = [ ...queries.value ];
    const index = newQueries.findIndex(q => q.id === newQuery.id);
    if (index === -1)
        return;

    newQueries[index] = newQuery;
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
            Saved Queries
        </h1>
        <div class="row">
            <div class="col-6 d-flex flex-column gap-4">
                <template v-if="queries">
                    <QueryDisplay
                        v-for="query in queries"
                        :key="query.id"
                        :query="query"
                        @update-query="updateQuery"
                        @delete-query="() => queries = queries?.filter(q => q.id !== query.id)"
                    />
                    <div v-if="queries.length === 0">
                        You have no saved queries yet. Go to <RouterLink :to="{ name: 'query' }">
                            Querying
                        </RouterLink> to add some!
                    </div>
                </template>
                <ResourceLoader :loading-function="fetchQueries" />
            </div>
            <div class="col-6">
                <div class="responsive-evocat-display">
                    <EvocatDisplay @evocat-created="evocatCreated" />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.responsive-evocat-display {
    --schema-category-canvas-width: 100%;
    position: sticky;
    top: 74px;
}
</style>
