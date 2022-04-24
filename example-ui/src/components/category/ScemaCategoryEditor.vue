<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import GraphDisplay from './GraphDisplay.vue';
import EditorForSchemaCategory from './edit/EditorForSchemaCategory.vue';

export default defineComponent({
    components: {
        GraphDisplay,
        EditorForSchemaCategory
    },
    data() {
        return {
            graph: null as Graph | null
        };
    },
    methods: {
        cytoscapeCreated(graph: Graph) {
            this.graph = graph;
        }
    }
});
</script>

<template>
    <div class="divide">
        <GraphDisplay @graph:created="cytoscapeCreated" />
        <div
            v-if="graph"
        >
            <EditorForSchemaCategory
                :graph="graph"
            />
        </div>
    </div>
</template>

<style scoped>
.divide {
    display: flex;
}
</style>
