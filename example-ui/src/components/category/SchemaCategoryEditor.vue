<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import GraphDisplay from './GraphDisplay.vue';
import EditorForSchemaCategory from './edit/EditorForSchemaCategory.vue';
import type { SchemaCategory } from '@/types/schema';

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
        },
        schemaCategorySaved(schemaCategory: SchemaCategory) {
            this.graph = null;
            const graphDisplay = this.$refs.graphDisplay as InstanceType<typeof GraphDisplay>;
            graphDisplay.updateSchema(schemaCategory);
        }
    }
});
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
