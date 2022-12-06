<script lang="ts">
import { RootProperty } from '@/types/accessPath/graph';
import { StaticName } from '@/types/identifiers';
import type { Node, Graph } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import GraphDisplay from '@/components/category/GraphDisplay.vue';
import NodeInput from './input/NodeInput.vue';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { LogicalModelFull } from '@/types/logicalModel';
import { getSchemaCategoryId } from '@/utils/globalSchemaSettings';
import API from '@/utils/api';

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
            logicalModels: [] as LogicalModelFull[],
            selectedLogicalModel: null as LogicalModelFull | null
        };
    },
    computed: {
        databaseAndRootNodeValid(): boolean {
            return !!this.selectedLogicalModel && !!this.selectingRootNode;
        }
    },
    async mounted() {
        const result = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId: getSchemaCategoryId() });
        if (result.status)
            this.logicalModels = result.data.map(LogicalModelFull.fromServer);
    },
    methods: {
        cytoscapeCreated(graph: Graph) {
            this.graph = graph;
        },
        confirmDatabaseAndRootNode() {
            if (!this.selectedLogicalModel || !this.selectingRootNode)
                return;

            this.selectingRootNode.unselect();
            this.selectingRootNode.becomeRoot();
            const label = this.selectingRootNode.schemaObject.label.toLowerCase();
            this.accessPath = new RootProperty(StaticName.fromString(label), this.selectingRootNode);
        },
        async createMapping(label: string) {
            if (! this.selectedLogicalModel || !this.graph || !this.accessPath)
                return;

            const result = await API.mappings.createNewMapping({}, {
                logicalModelId: this.selectedLogicalModel.id,
                rootObjectId: this.accessPath.node.schemaObject.id,
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
                    v-if="!accessPath || !selectedLogicalModel"
                    class="editor"
                >
                    <table>
                        <tr>
                            <td class="label">
                                Logical model:
                            </td>
                            <td class="value">
                                <select v-model="selectedLogicalModel">
                                    <option
                                        v-for="logicalModel in logicalModels"
                                        :key="logicalModel.id"
                                        :value="logicalModel"
                                    >
                                        {{ logicalModel.label }}
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
                            :disabled="!selectedLogicalModel || !selectingRootNode"
                            @click="confirmDatabaseAndRootNode"
                        >
                            Confirm
                        </button>
                    </div>
                </div>
                <AccessPathEditor
                    v-else
                    :graph="graph"
                    :database="selectedLogicalModel.database"
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
