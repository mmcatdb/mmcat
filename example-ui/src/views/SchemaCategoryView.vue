<script lang="ts">
import { defineComponent } from 'vue';
import { GET, PUT } from '@/utils/backendAPI';
import { SchemaCategoryFromServer, SchemaCategory, PositionUpdateToServer } from '@/types/schema';
import cytoscape, { type Core } from 'cytoscape';
import type { ElementDefinition } from 'cytoscape';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import AccessPathGraphicEditor from '../components/category/AccessPathGraphicEditor.vue';

export default defineComponent({
    components: {
    ResourceNotFound,
    ResourceLoading,
    AccessPathGraphicEditor
},
    props: {},
    data() {
        return {
            schema: null as SchemaCategory | null,
            schemaFetched: false,
            saveButtonDisabled: false,
            cy: null as Core | null
        };
    },
    async mounted() {
        /*
        const result = await GET<any>(`/schemaCategories/1`);
        if (result.status && 'data' in result) {
            console.log(result.data);
            this.schema = result.data;

            const objects = result.data.objects.map((object: any) => SchemaObject.fromServer(object));
            const morphisms = result.data.morphisms.map((morphism: any) => SchemaMorphism.fromServer(morphism));

            console.log(objects);
            console.log(morphisms);

            const cy = this.createCytoscape(objects, morphisms);
        }
        */

        const result = await GET<SchemaCategoryFromServer>(`/schemaCategories/1`);
        if (result.status && 'data' in result) {
            console.log(result.data);
            this.schema = SchemaCategory.fromServer(result.data);

            this.cy = this.createCytoscape(this.schema);
        }

        this.schemaFetched = true;
    },
    methods: {
        createCytoscape(schema: SchemaCategory) {
            const elements = [] as ElementDefinition[];

            schema.objects.forEach(object => elements.push({
                data: {
                    id: object.id.toString(),
                    label: JSON.parse(object.jsonValue).label
                },
                position: object.position
            }));
            schema.morphisms.filter(morphism => morphism.isBase).forEach(morphism => elements.push({ data: {
                id: 'm' + morphism.id.toString(),
                source: morphism.domId,
                target: morphism.codId
            } }));

            console.log(elements);
            console.log(document.getElementById('cytoscape'));

            return cytoscape({
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
                    }
                ]
            });
        },
        async savePositionChanges() {
            this.saveButtonDisabled = true;
            console.log('Saving position changes');

            const updatedPositions = this.schema?.objects
                .map(object => object.toPositionUpdateToServer())
                .filter(update => update != null);
            const result = await PUT<PositionUpdateToServer[]>(`/schemaCategories/positions/${this.schema?.id}`, updatedPositions);
            console.log('UPDATE RESULT:', result);

            this.saveButtonDisabled = false;
        }
    }
});
</script>

<template>
    <h1>There is a schema category</h1>
    <button
        :disabled="saveButtonDisabled"
        @click="savePositionChanges"
    >
        Uložit změny
    </button>
    <div
        id="cytoscape"
    />
    <template v-if="schema">
        <p>
            Fetched!
        </p>
        <AccessPathGraphicEditor :cytoscape="cy" />
    </template>
    <ResourceNotFound v-else-if="schemaFetched" />
    <ResourceLoading v-else />
</template>

<style scoped>
#cytoscape {
    width: 1600px;
    height: 500px;
    background-color: whitesmoke;
}
</style>
