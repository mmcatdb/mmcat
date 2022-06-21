<script lang="ts">
import { defineComponent } from 'vue';
import { GET, PUT } from '@/utils/backendAPI';
import { SchemaCategoryFromServer, SchemaCategory, PositionUpdateToServer } from '@/types/schema';
import cytoscape from 'cytoscape';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { Graph } from '@/types/categoryGraph';
import { style } from './defaultGraphStyle';
import { type MappingFromServer, Mapping } from '@/types/mapping';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading
    },
    emits: [ 'graph:created' ],
    data() {
        return {
            schemaCategory: null as SchemaCategory | null,
            schemaFetched: false,
            saveButtonDisabled: false,
            graph: null as Graph | null
        };
    },
    async mounted() {
        const result = await GET<SchemaCategoryFromServer>(`/schemaCategories/1`);
        const mappingsResult = await GET<MappingFromServer[]>('/mappings');

        if (result.status && mappingsResult.status) {
            console.log(result.data);
            this.schemaCategory = SchemaCategory.fromServer(result.data);
            const mappings = mappingsResult.data.map(mappingFromServer => Mapping.fromServer(mappingFromServer));

            this.graph = this.createGraph(this.schemaCategory, mappings);

            this.schemaFetched = true;
            this.$emit('graph:created', this.graph);
        }
    },
    methods: {
        createGraph(schema: SchemaCategory, mappings: Mapping[]): Graph {
            const container = document.getElementById('cytoscape');

            // This is needed because of some weird bug.
            // It has to do something with the cache (because it doesn't appear after hard refresh).
            // It causes the cytoscape div to contain two cytoscape canvases (the first one is empty, probably it's here from the previous instance).
            // Weird is this only occurs after 'build', not 'dev' (meaning 'serve').
            if (container) {
                var child = container.lastElementChild;
                while (child) {
                    container.removeChild(child);
                    child = container.lastElementChild;
                }
            }

            const cytoscapeInstance = cytoscape({
                container,
                layout: { name: 'preset' },
                //elements,
                style
            });

            mappings.forEach(mapping => schema.setDatabaseToObjectsFromMapping(mapping));


            const graph = new Graph(cytoscapeInstance, schema);

            schema.objects.forEach(object => graph.createNode(object));

            // First we create a dublets of morphisms. Then we create edges from them.
            const sortedBaseMorphisms = schema.morphisms.filter(morphism => morphism.isBase)
                .sort((m1, m2) => m1.sortBaseValue - m2.sortBaseValue);
            const morphismDublets = [];
            //for (let i = 0; i < sortedBaseMorphisms.length; i += 2)
            for (let i = 0; i < sortedBaseMorphisms.length; i += 2)
                morphismDublets.push({ morphism: sortedBaseMorphisms[i], dualMorphism: sortedBaseMorphisms[i + 1] });

            morphismDublets.forEach(dublet => graph.createEdgeWithDual(dublet.morphism));

            // Position the object to the center of the canvas.
            graph.center();

            return graph;
        },
        async savePositionChanges() {
            this.saveButtonDisabled = true;
            console.log('Saving position changes');

            const updatedPositions = this.schemaCategory?.objects
                .map(object => object.toPositionUpdateToServer())
                .filter(update => update != null);
            const result = await PUT<PositionUpdateToServer[]>(`/schemaCategories/positions/${this.schemaCategory?.id}`, updatedPositions);
            console.log('UPDATE RESULT:', result);

            this.saveButtonDisabled = false;
        }
    }
});
</script>

<template>
    <div class="graph-display">
        <div
            id="cytoscape"
        />
        <template v-if="schemaCategory">
            <div class="category-command-panel">
                <button
                    :disabled="saveButtonDisabled"
                    @click="savePositionChanges"
                >
                    Save positions
                </button>
            </div>
        </template>
        <ResourceNotFound v-else-if="schemaFetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
#cytoscape {
    width: var(--schema-category-canvas-width);
    height: var(--schema-category-canvas-height);
    background-color: var(--color-background-canvas);
}

.graph-display {
    display: flex;
    flex-direction: column;
    margin-right: 16px;
}

.category-command-panel {
    padding: 8px 8px;
    background-color: var(--color-background-dark);
}
</style>
