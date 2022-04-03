<script lang="ts">
import { RootProperty } from '@/types/accessPath';
import { StaticName } from '@/types/identifiers';
import type { SchemaCategory } from '@/types/schema';
import type { NodeSchemaData } from '@/types/categoryGraph';
import type { Core } from 'cytoscape';
import { defineComponent } from 'vue';
import SchemaCategoryGraph from '../category/SchemaCategoryGraph.vue';
import SelectRoot from './SelectRoot.vue';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { GET } from '@/utils/backendAPI';
import { Database, type DatabaseFromServer } from '@/types/database';

export default defineComponent({
    components: {
        SchemaCategoryGraph,
        SelectRoot,
        AccessPathEditor
    },
    data() {
        return {
            //schemaCategory: null as SchemaCategory | null,
            cytoscape: null as Core | null,
            accessPath: null as RootProperty | null,
            rootObjectName: 'pathName',
            rootNodeData: null as NodeSchemaData | null,
        };
    },
    async mounted() {
        // TODO
        const result = await GET<DatabaseFromServer[]>('/databases');
        if (result.status) {
            const databases = result.data.map(databaseFromServer => new Database(databaseFromServer));
            console.log(databases);
        }
    },
    methods: {
        cytoscapeCreated(cytoscape: Core, schemaCategory: SchemaCategory) {
            this.cytoscape = cytoscape;
            this.schemaCategory = schemaCategory;
        },
        onRootNodeSelect(data: NodeSchemaData) {
            const name = data.schemaObject.label;
            this.accessPath = new RootProperty(StaticName.fromString(name));
            this.rootObjectName = name;
            this.rootNodeData = data;
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
