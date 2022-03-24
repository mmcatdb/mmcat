<script lang="ts">
import { defineComponent } from 'vue';
import { GET, PUT } from '@/utils/backendAPI';
import { SchemaCategoryFromServer, SchemaCategory, PositionUpdateToServer } from '@/types/schema';
import cytoscape, { type Core } from 'cytoscape';
import type { ElementDefinition } from 'cytoscape';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading
    },
    emits: [ 'cytoscape:ready' ],
    data() {
        return {
            schemaCategory: null as SchemaCategory | null,
            schemaFetched: false,
            saveButtonDisabled: false,
            cytoscapeInstance: null as Core | null
        };
    },
    async mounted() {
        const result = await GET<SchemaCategoryFromServer>(`/schemaCategories/1`);
        if (result.status && 'data' in result) {
            console.log(result.data);
            this.schemaCategory = SchemaCategory.fromServer(result.data);

            this.cytoscapeInstance = this.createCytoscape(this.schemaCategory);
        }

        this.schemaFetched = true;
        this.$emit('cytoscape:ready', this.cytoscapeInstance, this.schemaCategory);
    },
    methods: {
        createCytoscape(schema: SchemaCategory) {
            const elements = [] as ElementDefinition[];

            schema.objects.forEach(object => elements.push({
                data: {
                    id: object.id.toString(),
                    label: object.label,
                    schemaObject: object
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
