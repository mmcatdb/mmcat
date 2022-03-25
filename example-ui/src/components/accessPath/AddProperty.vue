<script lang="ts">
import { ComplexProperty } from '@/types/accessPath';
import type { NodeSchemaData, NodeSequence } from '@/types/categoryGraph';
import { StaticName, type Signature } from '@/types/identifiers';
import type { Core, EventObject, NodeSingular } from 'cytoscape';
import { defineComponent } from 'vue';
import SelectPathInGraph from './SelectPathInGraph.vue';

export default defineComponent({
    components: { SelectPathInGraph },
    props: {
        cytoscape: {
            type: Object as () => Core,
            required: true
        },
        propertyRootNode: {
            type: Object as () => NodeSchemaData,
            required: true
        }
    },
    emits: [ 'property:add' ],
    data() {
        return {
            //propertyRootNode: null as NodeSchemaData | null,
            lastClickedNode: null as NodeSchemaData | null,
            signature: null as Signature | null,
            choosingSignature: false
        };
    },
    methods: {
        addNewProperty() {
            const property = new ComplexProperty(StaticName.fromString('todo'), this.signature || Signature.null);
            this.$emit('property:add', property);
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Add property</h2>
        <label>Name:</label><br>
        <label>Signature: {{ signature }} </label>
        <SelectPathInGraph
            v-if="choosingSignature"
            :cytoscape="cytoscape"
            :root-node="propertyRootNode"
            @path-in-graph:confirm="choosingSignature = false"
            @path-in-graph:cancel="choosingSignature = false; signature = null"
            @path-in-graph:update="(sequence) => signature = sequence.toCompositeSignature()"
        />
        <button
            v-else
            @click="choosingSignature = true"
        >
            Start
        </button>
        <br>
        <label>Value?:</label>
        <br>
        <button @click="addNewProperty">
            Add property
        </button>
    </div>
</template>

<style scoped>
.outer {

}
</style>

