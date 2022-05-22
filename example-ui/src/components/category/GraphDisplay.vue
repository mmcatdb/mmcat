<script lang="ts">
import { defineComponent } from 'vue';
import { GET, PUT } from '@/utils/backendAPI';
import { SchemaCategoryFromServer, SchemaCategory, PositionUpdateToServer } from '@/types/schema';
import cytoscape from 'cytoscape';
import type { ElementDefinition } from 'cytoscape';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { Graph, Node } from '@/types/categoryGraph';
import { style } from './defaultGraphStyle';

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
        if (result.status && 'data' in result) {
            console.log(result.data);
            this.schemaCategory = SchemaCategory.fromServer(result.data);

            const cytoscapeInstance = this.createCytoscape(this.schemaCategory);
            this.graph = new Graph(cytoscapeInstance, this.schemaCategory);

            this.schemaFetched = true;
            this.$emit('graph:created', this.graph);
        }
    },
    methods: {
        createCytoscape(schema: SchemaCategory) {
            const elements = [] as ElementDefinition[];

            const nodes = [] as Node[];
            schema.objects.forEach(object => {
                const node = new Node(object);
                nodes.push(node);

                const definition = {
                    data: {
                        id: object.id.toString(),
                        label: object.label,
                        schemaData: node
                    },
                    position: object.position
                };
                elements.push(definition);
            });

            schema.morphisms.filter(morphism => morphism.isBase)
                // This ensures the bezier morphism pairs have allways the same chirality.
                .sort((m1, m2) => m1.domId !== m2.domId ? m2.domId - m1.domId : m2.codId - m1.codId)
                .forEach(morphism => elements.push({ data: {
                    id: 'm' + morphism.id.toString(),
                    source: morphism.domId,
                    target: morphism.codId,
                    //label: ((value: string) => value.startsWith('-') ? undefined : value )(morphism.signature.toString())
                    label: morphism.signature.toString()
                } }));

            const container = document.getElementById('cytoscape');
            console.log(container);

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

            const output = cytoscape({
                container,
                layout: { name: 'preset' },
                elements,
                style
            });

            nodes.forEach(node => node.setCytoscapeNode(output.nodes('#' + node.schemaObject.id).first()));

            schema.morphisms.filter(morphism => morphism.isBase).forEach(morphism => {
                const domNode = nodes.find(node => node.schemaObject.id === morphism.domId);
                if (!domNode)
                    throw new Error(`Domain object node with id ${morphism.domId} not found for morphism ${morphism.signature.toString()}.`);

                const codNode = nodes.find(node => node.schemaObject.id === morphism.codId);
                if (!codNode)
                    throw new Error(`Codomain object node with id ${morphism.codId} not found for morphism ${morphism.signature.toString()}.`);

                domNode.addNeighbour(codNode, morphism);
                //codNode.addNeighbour(domNode, morphism); // This will be added by the dual morphism
            });

            return output;
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
                    Uložit změny
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
    height: 500px;
    background-color: var(--color-background-inverse);
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
