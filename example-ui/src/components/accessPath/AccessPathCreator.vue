<script lang="ts">
import { RootProperty } from '@/types/accessPath/graph';
import { StaticName } from '@/types/identifiers';
import type { Node, Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import GraphDisplay from '../category/GraphDisplay.vue';
import SelectRoot from './SelectRoot.vue';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { GET, POST } from '@/utils/backendAPI';
import { Database, type DatabaseFromServer } from '@/types/database';
import type { Mapping } from '@/types/mapping';

export default defineComponent({
    components: {
        GraphDisplay,
        SelectRoot,
        AccessPathEditor
    },
    data() {
        return {
            graph: null as Graph | null,
            accessPath: null as RootProperty | null,
            rootObjectName: 'pathName',
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
        cytoscapeCreated(graph: Graph) {
            this.graph = graph;
        },
        selectRootNode(node: Node) {
            const name = node.schemaObject.label;
            this.accessPath = new RootProperty(StaticName.fromString(name), node);
            this.rootObjectName = name;
        },
        async createMapping() {
            const result = await POST<Mapping>('/mappings', {
                id: null,
                databaseId: this.selectedDatabase?.id,
                categoryId: this.graph?.schemaCategory.id,
                rootObjectId: this.accessPath?.node.schemaObject.id,
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
        <GraphDisplay @graph:created="cytoscapeCreated" />
        <div
            v-if="graph"
            class="divide"
        >
            <div class="editor">
                <template v-if="!!selectedDatabase">
                    <div v-if="accessPath === null">
                        <SelectRoot
                            :graph="graph"
                            @root-node:confirm="selectRootNode"
                        />
                    </div>
                    <div v-else>
                        <AccessPathEditor
                            :graph="graph"
                            :database="selectedDatabase"
                            :root-property="accessPath"
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
