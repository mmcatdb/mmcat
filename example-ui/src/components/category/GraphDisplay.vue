<script lang="ts">
import { defineComponent, setBlockTracking } from 'vue';
import { GET, PUT } from '@/utils/backendAPI';
import { SchemaCategoryFromServer, SchemaCategory, PositionUpdateToServer } from '@/types/schema';
import cytoscape from 'cytoscape';
import type { ElementDefinition } from 'cytoscape';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import { Graph, Node } from '@/types/categoryGraph';

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

            schema.morphisms.filter(morphism => morphism.isBase).forEach(morphism => elements.push({ data: {
                id: 'm' + morphism.id.toString(),
                source: morphism.domId,
                target: morphism.codId,
                label: ((value: string) => value.startsWith('-') ? undefined : value )(morphism.signature.toString())
            } }));

            console.log(elements);
            console.log(document.getElementById('cytoscape'));

            const output = cytoscape({
                container: document.getElementById('cytoscape'),
                layout: { name: 'preset' },
                elements,
                style: [
                    {
                        selector: 'node',
                        style: {
                            'background-color': 'white',
                            'border-color': 'black',
                            'border-width': '1px',
                            label: 'data(label)'
                        }
                    },
                    {
                        selector: '.root',
                        style: {
                            'background-color': 'red'
                        }
                    },
                    {
                        selector: '.available',
                        style: {
                            'border-color': 'greenyellow',
                            'border-width': '4px',
                        }
                    },
                    {
                        selector: '.certainlyAvailable',
                        style: {
                            'border-color': 'darkgreen',
                            'border-width': '4px',
                        }
                    },
                    {
                        selector: '.maybe',
                        style: {
                            'border-color': 'orange',
                            'border-width': '4px',
                        }
                    },
                    {
                        selector: '.selected',
                        style: {
                            'border-color': 'blue',
                            'border-width': '4px',
                        }
                    },
                    {
                        selector: "edge[label]",
                        style: {
                            "font-weight": "bold",
                            label: 'data(label)'
                        }
                    }
                ]
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
    <div class="outer">
        <div>
            <button
                :disabled="saveButtonDisabled"
                @click="savePositionChanges"
            >
                Uložit změny
            </button>
        </div>
        <div
            id="cytoscape"
        />
        <template v-if="schemaCategory">
            Schema category fetched!
        </template>
        <ResourceNotFound v-else-if="schemaFetched" />
        <ResourceLoading v-else />
    </div>
</template>

<style scoped>
#cytoscape {
    width: 800px;
    height: 500px;
    background-color: whitesmoke;
}

.outer {
    display: flex;
    flex-direction: column;
}
</style>
