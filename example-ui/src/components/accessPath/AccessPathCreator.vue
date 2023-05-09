<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { GraphRootProperty } from '@/types/accessPath/graph';
import { SignatureId, StaticName } from '@/types/identifiers';
import { type Node, type Graph, SelectionType } from '@/types/categoryGraph';
import GraphDisplay from '@/components/category/GraphDisplay.vue';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryInfo, useSchemaCategoryId } from '@/utils/injects';
import API from '@/utils/api';
import { useRoute, useRouter } from 'vue-router';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SingleNodeInput from '@/components/input/SingleNodeInput.vue';
import { Evocat } from '@/types/evocat/Evocat';

const route = useRoute();
const router = useRouter();

const graph = ref<Graph>();
const evocat = ref<Evocat>();
const accessPath = ref<GraphRootProperty>();
const selectingRootNode = ref<Node>();
const logicalModels = ref<LogicalModel[]>([]);
const selectedLogicalModel = ref<LogicalModel>();

const databaseAndRootNodeValid = computed(() => {
    return !!selectedLogicalModel.value && !!selectingRootNode.value;
});

const categoryId = useSchemaCategoryId();
const category = useSchemaCategoryInfo();

onMounted(async () => {
    const result = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    if (result.status) {
        logicalModels.value = result.data.map(LogicalModel.fromServer);
        selectedLogicalModel.value = logicalModels.value.find(model => model.id.toString() === route.query.logicalModelId);
    }
});

function cytoscapeCreated(newGraph: Graph) {
    graph.value = newGraph;
    evocat.value = Evocat.create(newGraph);
}

function confirmDatabaseAndRootNode() {
    if (!selectedLogicalModel.value || !selectingRootNode.value)
        return;

    selectingRootNode.value.unselect();
    selectingRootNode.value.becomeRoot();
    const label = selectingRootNode.value.schemaObject.label.toLowerCase();
    accessPath.value = new GraphRootProperty(StaticName.fromString(label), selectingRootNode.value);
}

async function createMapping(primaryKey: SignatureId) {
    if (! selectedLogicalModel.value || !graph.value || !accessPath.value)
        return;

    const result = await API.mappings.createNewMapping({}, {
        logicalModelId: selectedLogicalModel.value.id,
        rootObjectKey: accessPath.value.node.schemaObject.key,
        primaryKey: new SignatureId(selectedLogicalModel.value.database.configuration.isSchemaLess ? [] : primaryKey.signatures).toServer(),
        kindName: accessPath.value.name.toString(),
        accessPath: accessPath.value.toServer(),
        categoryVersion: category.value.version,
    });
    if (result.status)
        router.push({ name: 'logicalModel', params: { id: selectedLogicalModel.value.id } });
}
</script>

<template>
    <div class="divide">
        <GraphDisplay @create:graph="cytoscapeCreated" />
        <div v-if="evocat">
            <div>
                <div
                    v-if="!accessPath || !selectedLogicalModel"
                    class="editor"
                >
                    <ValueContainer>
                        <ValueRow label="Logical model:">
                            <select v-model="selectedLogicalModel">
                                <option
                                    v-for="logicalModel in logicalModels"
                                    :key="logicalModel.id"
                                    :value="logicalModel"
                                >
                                    {{ logicalModel.label }}
                                </option>
                            </select>
                        </ValueRow>
                        <ValueRow label="Root object:">
                            <SingleNodeInput
                                v-model="selectingRootNode"
                                :type="SelectionType.Root"
                            />
                        </ValueRow>
                    </ValueContainer>
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
