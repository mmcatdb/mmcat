<script lang="ts">
import { defineComponent } from 'vue';
import type { Node } from '@/types/categoryGraph';
import type { Graph } from '@/types/categoryGraph';
import InstanceObject from './InstanceObject.vue';
import GraphDisplay from './GraphDisplay.vue';

export default defineComponent({
    components: {
        GraphDisplay,
        InstanceObject
    },
    data() {
        return {
            graph: null as Graph | null,
            selectedNode: null as Node | null
        };
    },
    methods: {
        cytoscapeCreated(graph: Graph) {
            this.graph = graph;

            graph.addNodeListener('tap', node => {
                this.selectedNode = node;
            });
        }
    }
});
</script>

<template>
    <div class="divide">
        <GraphDisplay @create:graph="cytoscapeCreated" />
        <InstanceObject
            v-if="selectedNode"
            :node="selectedNode"
        />
    </div>
</template>

<style scoped>

</style>
