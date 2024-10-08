<script setup lang="ts">
import { computed, onMounted, provide, ref, shallowRef } from 'vue';
import { GraphRootProperty } from '@/types/accessPath/graph';
import { SignatureId, StaticName } from '@/types/identifiers';
import { type Node, type Graph, SelectionType } from '@/types/categoryGraph';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryInfo, useSchemaCategoryId, evocatKey, type EvocatContext } from '@/utils/injects';
import API from '@/utils/api';
import { useRoute, useRouter } from 'vue-router';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SingleNodeInput from '@/components/input/SingleNodeInput.vue';
import type { Evocat } from '@/types/evocat/Evocat';
import EvocatDisplay from '../category/EvocatDisplay.vue';

const route = useRoute();
const router = useRouter();

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();
provide(evocatKey, { evocat, graph } as EvocatContext);

function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    graph.value = context.graph;
}

const accessPath = ref<GraphRootProperty>();
const selectingRootNode = ref<Node>();
const logicalModels = shallowRef<LogicalModel[]>([]);
const selectedLogicalModel = shallowRef<LogicalModel>();

const datasourceAndRootNodeValid = computed(() => !!selectedLogicalModel.value && !!selectingRootNode.value);

const categoryId = useSchemaCategoryId();
const category = useSchemaCategoryInfo();

onMounted(async () => {
    const result = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    if (result.status) {
        logicalModels.value = result.data.map(LogicalModel.fromServer);
        selectedLogicalModel.value = logicalModels.value.find(model => model.id.toString() === route.query.logicalModelId);
    }
});

function confirmDatasourceAndRootNode() {
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
        rootObjectKey: accessPath.value.node.schemaObject.key.toServer(),
        primaryKey: new SignatureId(selectedLogicalModel.value.datasource.configuration.isSchemaless ? [] : primaryKey.signatures).toServer(),
        kindName: accessPath.value.name.toString(),
        accessPath: accessPath.value.toServer(),
        categoryVersionn: category.value.versionId,
    });
    if (result.status)
        router.push({ name: 'logicalModel', params: { id: selectedLogicalModel.value.id } });
}
</script>

<template>
    <div class="divide">
        <EvocatDisplay @evocat-created="evocatCreated" />
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
                            @click="confirmDatasourceAndRootNode"
                        >
                            Confirm
                        </button>
                    </div>
                </div>
                <AccessPathEditor
                    v-else
                    :datasource="selectedLogicalModel.datasource"
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
