<script setup lang="ts">
import { ref, shallowRef } from 'vue';
import NewQueryDisplay, { type QueryLog } from '@/components/query/NewQueryDisplay.vue';
import type { Evocat } from '@/types/evocat/Evocat';
import type { Graph } from '@/types/categoryGraph';
import EvocatDisplay from '@/components/category/EvocatDisplay.vue';
import type { QueryResult } from '@/utils/api/routes/queries';
import type { Result } from '@/types/api/result';

const logs = ref<QueryLog[]>([]);

let lastQueryId = 0;

function queryExecuted(content: string, result: Result<QueryResult>) {
    logs.value.unshift({ id: lastQueryId++, content, result });
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
        <h1>Querying</h1>
        <div class="row">
            <div class="col-6 d-flex flex-column gap-4">
                <NewQueryDisplay @execute-query="queryExecuted" />
                <NewQueryDisplay
                    v-for="log in logs"
                    :key="log.id"
                    :initial-data="log"
                />
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
