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
import AccessPathEditor from './AccessPathEditor.vue';

enum State {
    Default,
    RootSelected,
    AddComplexProperty,
    EditComplexProperty,
    AddSimpleProperty,
    EditSimpleProperty
}

export default defineComponent({
    components: {
        SchemaCategoryGraph,
        SelectRoot,
        ComplexPropertyDisplay,
        EditComplexProperty,
        AccessPathEditor
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
                <div v-if="accessPath === null || rootNodeData === null">
                    <SelectRoot
                        :cytoscape="cytoscape"
                        @root-node:confirm="onRootNodeSelect"
                    />
                </div>
                <div v-else>
                    <AccessPathEditor
                        v-if="accessPath !== null && rootNodeData !== null"
                        :cytoscape="cytoscape"
                        :root-node="rootNodeData"
                        :access-path="accessPath"
                    />
                </div>
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
