<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RootProperty } from '@/types/accessPath/graph';
import { StaticName } from '@/types/identifiers';
import type { Node, Graph } from '@/types/categoryGraph';
import GraphDisplay from '@/components/category/GraphDisplay.vue';
import NodeInput from './input/NodeInput.vue';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategory } from '@/utils/globalSchemaSettings';
import API from '@/utils/api';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const graph = ref<Graph>();
const accessPath = ref<RootProperty>();
const selectingRootNode = ref<Node>();
const logicalModels = ref<LogicalModel[]>([]);
const selectedLogicalModel = ref<LogicalModel>();

const databaseAndRootNodeValid = computed(() => {
    return !!selectedLogicalModel.value && !!selectingRootNode.value;
});

const schemaCategoryId = useSchemaCategory();

onMounted(async () => {
    const result = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId: schemaCategoryId });
    if (result.status) {
        logicalModels.value = result.data.map(LogicalModel.fromServer);
        selectedLogicalModel.value = logicalModels.value.find(model => model.id.toString() === route.params.logicalModelId);
    }
});


function cytoscapeCreated(newGraph: Graph) {
    graph.value = newGraph;
}

function confirmDatabaseAndRootNode() {
    if (!selectedLogicalModel.value || !selectingRootNode.value)
        return;

    selectingRootNode.value.unselect();
    selectingRootNode.value.becomeRoot();
    const label = selectingRootNode.value.schemaObject.label.toLowerCase();
    accessPath.value = new RootProperty(StaticName.fromString(label), selectingRootNode.value);
}

async function createMapping(label: string) {
    if (! selectedLogicalModel.value || !graph.value || !accessPath.value)
        return;

    const result = await API.mappings.createNewMapping({}, {
        logicalModelId: selectedLogicalModel.value.id,
        rootObjectId: accessPath.value.node.schemaObject.id,
        jsonValue: JSON.stringify({
            label: label
        }),
        mappingJsonValue: JSON.stringify({
            kindName: accessPath.value?.name.toString(),
            pkey: [], // TODO this is important for the IC algorithm
            accessPath: accessPath.value?.toJSON()
        })
    });
    if (result.status)
        router.push({ name: 'logicalModel', params: { id: selectedLogicalModel.value.id } });
}
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
