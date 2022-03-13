<script lang="ts">
import { defineComponent } from 'vue';
import { GET } from '@/utils/backendAPI';
import { SchemaObject, SchemaMorphism, SchemaCategoryFromServer, SchemaCategory } from '@/types/schema';
import cytoscape from 'cytoscape';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading
    },
    props: {},
    data() {
        return {
            schema: null as SchemaCategory | null,
            schemaFetched: false
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

            const cy = this.createCytoscape(this.schema);
        }

        this.schemaFetched = true;
    },
    methods: {
        createCytoscape(schema: SchemaCategory) {
            const elements = [] as any[];

            schema.objects.forEach(object => elements.push({ data: {
                id: object.id,
                label: object.jsonValue
            } }));
            schema.morphisms.forEach(morphism => elements.push({ data: {
                id: morphism.id,
                source: morphism.domId,
                target: morphism.codId
            } }));

            console.log(document.getElementById('cytoscape'));

            return cytoscape({
                container: document.getElementById('cytoscape'),
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
        }
    }
});
</script>

<template>
    <h1>There is a schema category</h1>
    <div
        id="cytoscape"
    />
    <p v-if="schema">
        Fetched!
    </p>
    <ResourceNotFound v-else-if="schemaFetched" />
    <ResourceLoading v-else />
</template>

<style scoped>
#cytoscape {
    width: 1600px;
    height: 700px;
    background-color: whitesmoke;
}
</style>
