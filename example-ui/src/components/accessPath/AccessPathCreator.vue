<script lang="ts">
import { RootProperty } from '@/types/accessPath/graph';
import { StaticName } from '@/types/identifiers';
import type { Node, Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import GraphDisplay from '@/components/category/GraphDisplay.vue';
import NodeInput from './input/NodeInput.vue';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { GET, POST } from '@/utils/backendAPI';
import { DatabaseView, type DatabaseViewFromServer } from '@/types/database';
import type { MappingFromServer } from '@/types/mapping';

export default defineComponent({
    components: {
        GraphDisplay,
        NodeInput,
        AccessPathEditor
    },
    data() {
        return {
            graph: null as Graph | null,
            accessPath: null as RootProperty | null,
            selectingRootNode: null as Node | null,
            databases: [] as DatabaseView[],
            selectingDatabase: null as DatabaseView | null,
            selectedDatabase: null as DatabaseView | null
        };
    },
    computed: {
        databaseAndRootNodeValid(): boolean {
            return !!this.selectingDatabase && !!this.selectingRootNode;
        }
    },
    async mounted() {
        const result = await GET<DatabaseViewFromServer[]>('/database-views');
        if (result.status)
            this.databases = result.data.map(DatabaseView.fromServer);
    },
    methods: {
        cytoscapeCreated(graph: Graph) {
            this.graph = graph;
        },
        confirmDatabaseAndRootNode() {
            if (!this.selectingDatabase || !this.selectingRootNode)
                return;

            this.selectedDatabase = this.selectingDatabase;

            this.selectingRootNode.unselect();
            this.selectingRootNode.becomeRoot();
            const label = this.selectingRootNode.schemaObject.label.toLowerCase();
            this.accessPath = new RootProperty(StaticName.fromString(label), this.selectingRootNode);
        },
        async createMapping(label: string) {
            const result = await POST<MappingFromServer>('/mappings', {
                databaseId: this.selectedDatabase?.id,
                categoryId: this.graph?.schemaCategory.id,
                rootObjectId: this.accessPath?.node.schemaObject.id,
                jsonValue: JSON.stringify({
                    label: label
                }),
                mappingJsonValue: JSON.stringify({
                    kindName: this.accessPath?.name.toString(),
                    pkey: [], // TODO this is important for the IC algorithm
                    accessPath: this.accessPath?.toJSON()
                })
            });
            if (result.status)
                this.$router.push({ name: 'mappings' });
        }
    }
});
</script>

<template>
    <div class="divide">
        <GraphDisplay @create:graph="cytoscapeCreated" />
        <div v-if="graph">
            <div>
                <div
                    v-if="!selectedDatabase || !accessPath"
                    class="editor"
                >
                    <table>
                        <tr>
                            <td class="label">
                                Database:
                            </td>
                            <td class="value">
                                <select v-model="selectingDatabase">
                                    <option
                                        v-for="database in databases"
                                        :key="database.id"
                                        :value="database"
                                    >
                                        {{ database.label }}
                                    </option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <td class="label">
                                Root object:
                            </td>
                            <td class="value">
                                <NodeInput
                                    v-model="selectingRootNode"
                                    :graph="graph"
                                />
                            </td>
                        </tr>
                    </table>
                    <div class="button-row">
                        <button
                            :disabled="!selectingDatabase || !selectingRootNode"
                            @click="confirmDatabaseAndRootNode"
                        >
                            Confirm
                        </button>
                    </div>
                </div>
                <AccessPathEditor
                    v-else
                    :graph="graph"
                    :database="selectedDatabase"
                    :root-property="accessPath"
                    @finish="createMapping"
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
