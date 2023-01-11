<script setup lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { ref } from 'vue';
import GraphDisplay from './GraphDisplay.vue';
import EditorForSchemaCategory from './edit/EditorForSchemaCategory.vue';
import type { SchemaCategory } from '@/types/schema';

const graph = ref<Graph>();

function cytoscapeCreated(newGraph: Graph) {
    graph.value = newGraph;
}

const graphDisplay = ref<InstanceType<typeof GraphDisplay>>();

function schemaCategorySaved(schemaCategory: SchemaCategory) {
    graph.value = undefined;
    graphDisplay.value?.updateSchema(schemaCategory);
}
</script>

<template>
    <div class="divide">
        <GraphDisplay
            ref="graphDisplay"
            @create:graph="cytoscapeCreated"
        />
        <div
            v-if="graph"
        >
            <EditorForSchemaCategory
                :graph="graph"
                @save="schemaCategorySaved"
            />
        </div>
    </div>
</template>

<style scoped>

</style>
