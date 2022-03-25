<script lang="ts">
import { ComplexProperty, SimpleProperty } from '@/types/accessPath';
import { Signature, StaticName } from '@/types/identifiers';
import type { SchemaObject, SchemaCategory } from '@/types/schema';
import type { NodeSchemaData } from '@/types/categoryGraph';
import type { Core, NodeSingular } from 'cytoscape';
import { defineComponent } from 'vue';
import SchemaCategoryGraph from '../category/SchemaCategoryGraph.vue';
import AccessPathJsonDisplay from './AccessPathJsonDisplay.vue';
import SelectRoot from './SelectRoot.vue';
import AddProperty from './AddProperty.vue';
import ComplexPropertyDisplay from './display/ComplexPropertyDisplay.vue';
import EditComplexProperty from './EditComplexProperty.vue';

enum State {
    Default,
    RootSelected,
    EditComplexProperty
}

export default defineComponent({
    components: {
    SchemaCategoryGraph,
    SelectRoot,
    AddProperty,
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
            property: null as ComplexProperty | null
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
        addNewProperty(property: ComplexProperty): void {
            this.accessPath?.subpaths.push(property);
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
                <AddProperty
                    :cytoscape="cytoscape"
                    :property-root-node="rootNodeData"
                    @property:add="addNewProperty"
                />

                <EditComplexProperty
                    v-if="editingComplexProperty"
                    :cytoscape="cytoscape"
                    :property="property"
                    :property-root-node="rootNodeData"
                    @property:save="editPropertySave"
                />
            </div>
            <ComplexPropertyDisplay
                v-if="accessPath !== null"
                :property="accessPath"
                :is-last="true"
                @complex:click="complexPropertyClicked"
                @simple:click="simplePropertyClicked"
            />
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
