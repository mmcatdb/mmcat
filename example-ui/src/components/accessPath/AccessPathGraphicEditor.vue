<script lang="ts">
import { ComplexProperty, SimpleProperty } from '@/types/accessPath';
import { Signature, StaticName } from '@/types/identifiers';
import type { SchemaObject, SchemaCategory } from '@/types/schema';
import type { NodeSchemaData } from '@/types/categoryGraph';
import type { Core } from 'cytoscape';
import { defineComponent } from 'vue';
import SchemaCategoryGraph from '../category/SchemaCategoryGraph.vue';
import SelectRoot from './SelectRoot.vue';
import ComplexPropertyDisplay from './display/ComplexPropertyDisplay.vue';
import EditComplexProperty from './edit/EditComplexProperty.vue';

enum State {
    Default,
    RootSelected,
    EditComplexProperty
}

export default defineComponent({
    components: {
        SchemaCategoryGraph,
        SelectRoot,
        ComplexPropertyDisplay,
        EditComplexProperty
    },
    data() {
        return {
            //schemaCategory: null as SchemaCategory | null,
            cytoscape: null as Core | null,
            lastClickedObject: null as SchemaObject | null,
            accessPath: null as ComplexProperty | null,
            rootObjectName: 'pathName',
            rootNodeData: null as NodeSchemaData | null,
            state: State.Default,
            State: State,

            editingComplexProperty: false,
            property: null as ComplexProperty | null,

            addingProperty: false
        };
    },
    methods: {
        cytoscapeCreated(cytoscape: Core, schemaCategory: SchemaCategory) {
            this.cytoscape = cytoscape;
            this.schemaCategory = schemaCategory;
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
        startAddProperty() {
            this.addingProperty = true;
            this.property = new ComplexProperty(StaticName.fromString(''), Signature.empty);
        },
        //addNewProperty(property: ComplexProperty): void {
        addNewProperty(): void {
            //this.accessPath?.subpaths.push(property);
            this.accessPath?.subpaths.push(this.property);
            this.addingProperty = false;
        },
        complexPropertyClicked(property: ComplexProperty) {
            console.log(property);
            this.property = property;
            this.editingComplexProperty = true;
        },
        simplePropertyClicked(property: SimpleProperty) {
            console.log(property);
        },
        editPropertySave(): void {
            this.editingComplexProperty = false;
            this.property = null;
        },
        editPropertyCancel(): void {
            this.editingComplexProperty = false;
            this.property = null;
        }
    }
});
</script>

<template>
    <div class="divide">
        <SchemaCategoryGraph @cytoscape:ready="cytoscapeCreated" />
        <div
            v-if="cytoscape"
            class="divide"
        >
            <div class="editor">
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
                    />

                    <template v-if="addingProperty">
                        <EditComplexProperty
                            :cytoscape="cytoscape"
                            :property-node="rootNodeData"
                            :property="property"
                            :is-new="true"
                            @save="addNewProperty"
                            @cancel="() => addingProperty = false"
                        />
                    </template>
                    <template v-else>
                        <div class="createProperty">
                            <button @click="startAddProperty">
                                Create new Property
                            </button>
                        </div>
                    </template>

                    <EditComplexProperty
                        v-if="editingComplexProperty"
                        :cytoscape="cytoscape"
                        :property-node="rootNodeData"
                        :property="property"
                        @save="editPropertySave"
                        @cancel="editPropertyCancel"
                    />
                </div>
            </div>
            <div class="display">
                <ComplexPropertyDisplay
                    v-if="accessPath !== null"
                    :property="accessPath"
                    :is-last="true"
                    @complex:click="complexPropertyClicked"
                    @simple:click="simplePropertyClicked"
                />
            </div>
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

.divide {
    display: flex;
}

.display {
    padding: 16px;
    margin: 16px;
}

.createProperty {
    padding: 16px;
    margin: 16px;
    border: 1px solid white;
}
</style>
