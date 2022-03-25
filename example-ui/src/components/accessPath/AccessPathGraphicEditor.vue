<script lang="ts">
import { ComplexProperty } from '@/types/accessPath';
import { Signature, StaticName } from '@/types/identifiers';
import type { SchemaObject, SchemaCategory } from '@/types/schema';
import { NodeSchemaData, SchemaObjectSequence } from '@/types/categoryGraph';
import type { Core, NodeSingular } from 'cytoscape';
import { defineComponent } from 'vue';
import SchemaCategoryGraph from '../category/SchemaCategoryGraph.vue';
import AccessPathJsonDisplay from './AccessPathJsonDisplay.vue';
import SelectRoot from './SelectRoot.vue';

enum State {
    Default,
    RootSelected,
}

export default defineComponent({
    components: {
        SchemaCategoryGraph,
        AccessPathJsonDisplay,
        SelectRoot
    },
    data() {
        return {
            //schemaCategory: null as SchemaCategory | null,
            cytoscape: null as Core | null,
            lastClickedObject: null as SchemaObject | null,
            accessPath: null as ComplexProperty | null,
            rootObjectName: 'pathName',
            rootNodeData: null as NodeSchemaData | null,
            choosingSignature: false,
            signaturePath: null as SchemaObjectSequence | null,
            chosenSignature: null as Signature | null,
            state: State.Default,
            State: State
        };
    },
    methods: {
        cytoscapeCreated(cytoscape: Core, schemaCategory: SchemaCategory) {
            this.cytoscape = cytoscape;
            this.schemaCategory = schemaCategory;

            this.cytoscape.addListener("tap", "node", (event) => {
                const node = event.target as NodeSingular;
                console.log("Tap on node:", node, node.id());
                this.lastClickedObject = this.schemaCategory?.objects.find(object => object.id == node.id()) || null;
                console.log(this.lastClickedObject);

                if (this.choosingSignature)
                    if (this.lastClickedObject) {
                        const result = this.signaturePath?.tryAddObject(this.lastClickedObject);
                        if (result)
                            node.style('background-color', 'yellow');
                    }

                node.toggleClass('selected');
                //node.addClass('selected');
                console.log('OBJECT:', node.data('schemaObject'));
            });
        },
        onRootNodeSelect(data: NodeSchemaData) {
            const name = data.schemaObject.label;
            this.accessPath = new ComplexProperty(StaticName.fromString(name), Signature.null);
            this.rootObjectName = name;
            this.rootNodeData = data;
            this.state = State.RootSelected;
        },
        rootNameInput() {
            if (this.accessPath.name instanceof StaticName)
                this.accessPath.name.value = this.rootObjectName;
        },
        startChoosingSignature() {
            this.choosingSignature = true;
            if (this.rootNodeData)
                this.signaturePath = new SchemaObjectSequence(this.rootNodeData.schemaObject);
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
        <div
            v-if="cytoscape"
            class="editor"
        >
            <div v-if="state === State.Default">
                <SelectRoot
                    :cytoscape="cytoscape"
                    @root-node:confirm="onRootNodeSelect"
                />
            </div>
            <div v-else>
                <label>Root object name:</label><br>
                <input
                    v-model="rootObjectName"
                    @input="rootNameInput"
                >
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
                    <button @click="confirmChoosingSignature">
                        Confirm
                    </button>
                    <button @click="cancelChoosingSignature">
                        Cancel
                    </button>
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
