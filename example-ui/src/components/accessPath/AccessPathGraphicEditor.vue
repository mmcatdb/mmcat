<script lang="ts">
import { ComplexProperty } from '@/types/accessPath';
import { Signature, StaticName } from '@/types/identifiers';
import { SchemaObject, type SchemaCategory } from '@/types/schema';
import { SchemaObjectSequence } from '@/types/categoryGraph';
import type { Core } from 'cytoscape';
import { defineComponent } from 'vue';
import SchemaCategoryGraph from '../category/SchemaCategoryGraph.vue';
import AccessPathJsonDisplay from './AccessPathJsonDisplay.vue';

export default defineComponent({
    components: { SchemaCategoryGraph, AccessPathJsonDisplay },
    data() {
        return {
            schemaCategory: null as SchemaCategory | null,
            cytoscape: null as Core | null,
            lastUpdate: 'nothing yet',
            lastClickedObject: null as SchemaObject | null,
            accessPath: null as ComplexProperty | null,
            rootObjectName: 'pathName',
            rootObject: null as SchemaObject | null,
            choosingSignature: false,
            signaturePath: null as SchemaObjectSequence | null,
            chosenSignature: null as Signature | null
        };
    },
    methods: {
        cytoscapeCreated(cytoscape: Core, schemaCategory: SchemaCategory) {
            this.cytoscape = cytoscape;
            this.schemaCategory = schemaCategory;

            this.cytoscape.addListener("tap", "node", (event) => {
                const node = event.target;
                console.log("Tap on node:", node, node.id());
                this.lastUpdate = node.id();
                this.lastClickedObject = this.schemaCategory?.objects.find(object => object.id == node.id()) || null;
                console.log(this.lastClickedObject);

                if (this.choosingSignature) {
                    if (this.lastClickedObject) {
                        const result = this.signaturePath?.tryAddObject(this.lastClickedObject);
                        if (result)
                            node.style('background-color', 'yellow');
                    }
                }
            });
        },
        chooseRootObjectConfirm() {
            const name = this.lastClickedObject?.label || 'NotFound';
            this.accessPath = new ComplexProperty(StaticName.fromString(name), Signature.null);
            this.cytoscape?.getElementById(`${this.lastClickedObject?.id}`).style('background-color', 'red');
            this.rootObjectName = name;
            this.rootObject = this.lastClickedObject;
            this.lastClickedObject = null;
        },
        rootNameInput() {
            if (this.accessPath.name instanceof StaticName)
                this.accessPath.name.value = this.rootObjectName;
        },
        startChoosingSignature() {
            this.choosingSignature = true;
            if (this.rootObject)
                this.signaturePath = new SchemaObjectSequence(this.rootObject);
        },
        confirmChoosingSignature() {
            this.choosingSignature = false;
            this.signaturePath?.objectIds.slice(1).forEach(id => this.cytoscape?.getElementById(id).style('background-color', 'white'));
            this.chosenSignature = this.signaturePath!.toCompositeSignature();
        },
        cancelChoosingSignature() {
            this.choosingSignature = false;
            this.signaturePath?.objectIds.slice(1).forEach(id => this.cytoscape?.getElementById(id).style('background-color', 'white'));
        },
        addNewProperty() {
            this.accessPath?.subpaths.push(new ComplexProperty(StaticName.fromString('todo'), this.chosenSignature || Signature.null));
        }
    }
});
</script>

<template>
<div class="divide">
    <SchemaCategoryGraph @cytoscape:ready="cytoscapeCreated" />
    <div class="editor">
        {{ lastUpdate }}
        <div v-if="!accessPath">
            Choose root object: {{ lastClickedObject?.label }}<br>
            <button
                @click="chooseRootObjectConfirm"
                :disabled="!lastClickedObject"
            >
                Confirm
            </button>
        </div>
        <div v-else>
            <label>Root object name:</label><br>
            <input v-model="rootObjectName" @input="rootNameInput">
            <br>
            <h2>Add property:</h2>
            <label>Name:</label><br>
            <label>Signature: {{ chosenSignature }} </label>
            <button
                v-if="!choosingSignature"
                @click="startChoosingSignature"
            >
                Start
            </button>
            <template v-else>
                <button @click="confirmChoosingSignature">Confirm</button>
                <button @click="cancelChoosingSignature">Cancel</button>
            </template>
            <br>
            <label>Value?:</label>
            <br>
            <button @click="addNewProperty">Add property</button>
        </div>
        <AccessPathJsonDisplay :accessPath="accessPath" />
    </div>
</div>
</template>

<style scoped>
.accessPathInput {
    color: white;
    background-color: black;
    width: 600px;
    height: 600px;
    font-size: 15px;
}

.editor {
    padding: 12px;
    display: flex;
    flex-direction: column;
}
</style>
