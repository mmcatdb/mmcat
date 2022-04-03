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
import { GET, POST } from '@/utils/backendAPI';
import { Database, type DatabaseFromServer } from '@/types/database';
import type { Mapping } from '@/types/mapping';

export default defineComponent({
    components: {
        SchemaCategoryGraph,
        SelectRoot,
        AccessPathEditor
    },
    data() {
        return {
            schemaCategory: null as SchemaCategory | null,
            cytoscape: null as Core | null,
            accessPath: null as RootProperty | null,
            rootObjectName: 'pathName',
            rootNodeData: null as NodeSchemaData | null,
            databases: [] as Database[],
            selectingDatabase: null as Database | null,
            selectedDatabase: null as Database | null,
            mappingName: 'new mapping'
        };
    },
    async mounted() {
        // TODO
        const result = await GET<DatabaseFromServer[]>('/databases');
        if (result.status)
            this.databases = result.data.map(databaseFromServer => new Database(databaseFromServer));
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
        },
        async createMapping() {
            const result = await POST<Mapping>('/mappings', {
                id: null,
                databaseId: this.selectedDatabase?.id,
                categoryId: this.schemaCategory?.id,
                rootObjectId: this.rootNodeData?.schemaObject.id,
                rootMorphismId: null, // TODO
                jsonValue: JSON.stringify({
                    name: this.mappingName
                }),
                mappingJsonValue: JSON.stringify({
                    kindName: this.accessPath?.name.toString(),
                    pkey: [], // TODO
                    accessPath: this.accessPath?.toJSON()
                })
            });
            console.log(result);
            if (result.status)
                this.$router.push({ name: 'jobs' });
        }
    }
});
</script>

<template>
    <label>Mapping name:</label>
    <input v-model="mappingName" />
    <div class="divide">
        <SchemaCategoryGraph @cytoscape:ready="cytoscapeCreated" />
        <div
            v-if="cytoscape"
            class="divide"
        >
            <div class="editor">
                <template v-if="!!selectedDatabase">
                    <div v-if="accessPath === null || rootNodeData === null">
                        <SelectRoot
                            :cytoscape="cytoscape"
                            @root-node:confirm="onRootNodeSelect"
                        />
                    </div>
                    <div v-else>
                        <AccessPathEditor
                            :cytoscape="cytoscape"
                            :database="selectedDatabase"
                            :root-node="rootNodeData"
                            :access-path="accessPath"
                        />
                        <button @click="createMapping">
                            Confirm and create mapping
                        </button>
                    </div>
                </template>
                <template v-else>
                    <label>Select database:</label>
                    <select v-model="selectingDatabase">
                        <option
                            v-for="database in databases"
                            :key="database.id"
                            :value="database"
                        >
                            {{ database.label }}
                        </option>
                    </select>
                    <button @click="selectedDatabase = selectingDatabase">
                        Confirm
                    </button>
                </template>
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
