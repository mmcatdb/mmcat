<script lang="ts">
import { RootProperty } from '@/types/accessPath/graph';
import { StaticName } from '@/types/identifiers';
import type { Node, Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import GraphDisplay from '@/components/category/GraphDisplay.vue';
import SelectRoot from './SelectRoot.vue';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { GET, POST } from '@/utils/backendAPI';
import { DatabaseView, type DatabaseViewFromServer } from '@/types/database';
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
            databases: [] as DatabaseView[],
            selectingDatabase: null as DatabaseView | null,
            selectedDatabase: null as DatabaseView | null
        };
    },
    async mounted() {
        // TODO
        const result = await GET<DatabaseViewFromServer[]>('/database-views');
        if (result.status)
            this.databases = result.data.map(databaseFromServer => new DatabaseView(databaseFromServer));
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
        async createMapping(name: string) {
            const result = await POST<Mapping>('/mappings', {
                id: null,
                databaseId: this.selectedDatabase?.id,
                categoryId: this.graph?.schemaCategory.id,
                rootObjectId: this.accessPath?.node.schemaObject.id,
                rootMorphismId: null, // TODO
                jsonValue: JSON.stringify({
                    name
                }),
                mappingJsonValue: JSON.stringify({
                    kindName: this.accessPath?.name.toString().toLowerCase(),
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
    <div class="divide">
        <GraphDisplay @graph:created="cytoscapeCreated" />
        <div v-if="graph">
            <div>
                <template v-if="!!selectedDatabase">
                    <SelectRoot
                        v-if="accessPath === null"
                        :graph="graph"
                        @root-node:confirm="selectRootNode"
                    />
                    <AccessPathEditor
                        v-else
                        :graph="graph"
                        :database="selectedDatabase"
                        :root-property="accessPath"
                        @finish="createMapping"
                    />
                </template>
                <div
                    v-else
                    class="editor"
                >
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
                    <div class="button-row">
                        <button @click="selectedDatabase = selectingDatabase">
                            Confirm
                        </button>
                    </div>
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
    display: flex;
    flex-direction: column;
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
