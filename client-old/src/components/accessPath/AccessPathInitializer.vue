<script setup lang="ts">
import { computed, onMounted, provide, ref, shallowRef } from 'vue';
import { GraphRootProperty } from '@/types/accessPath/graph';
import { SignatureId, StaticName } from '@/types/identifiers';
import { type Node, type Graph, SelectionType } from '@/types/categoryGraph';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryInfo, useSchemaCategoryId, evocatKey, type EvocatContext } from '@/utils/injects';
import API from '@/utils/api';
import { useRoute, useRouter } from 'vue-router';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SingleNodeInput from '@/components/input/SingleNodeInput.vue';
import type { Evocat } from '@/types/evocat/Evocat';
import EvocatDisplay from '../category/EvocatDisplay.vue';
import AccessPathLoader from './AccessPathLoader.vue';
import AccessPathCreator2 from '@/components/accessPath/AccessPathCreator2.vue';

const route = useRoute();
const router = useRouter();

const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();
provide(evocatKey, { evocat, graph } as EvocatContext);

function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    graph.value = context.graph;
}

const logicalModels = shallowRef<LogicalModel[]>([]);
const selectedLogicalModel = shallowRef<LogicalModel>();

const categoryId = useSchemaCategoryId();
const category = useSchemaCategoryInfo();

const initializeType = ref<'create' | 'load'>();

onMounted(async () => {
    const result = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    if (result.status) {
        logicalModels.value = result.data.map(LogicalModel.fromServer);
        selectedLogicalModel.value = logicalModels.value.find(model => model.id.toString() === route.query.logicalModelId);
    }
});

async function createMapping(primaryKey: SignatureId, accessPath: GraphRootProperty) {
    if (!selectedLogicalModel.value || !graph.value || !accessPath)
        return;

    const result = await API.mappings.createNewMapping({}, {
        logicalModelId: selectedLogicalModel.value.id,
        rootObjectKey: accessPath.node.schemaObject.key.toServer(),
        primaryKey: new SignatureId(selectedLogicalModel.value.datasource.configuration.isSchemaless ? [] : primaryKey.signatures).toServer(),
        kindName: accessPath.name.toString(),
        accessPath: accessPath.toServer(),
        categoryVersionn: category.value.versionId,
    });
    if (result.status)
        router.push({ name: 'logicalModel', params: { id: selectedLogicalModel.value.id } });
}

function setInitializeType(type: 'create' | 'load') {
    initializeType.value = type;
}
</script>

<template>
    <div class="divide">
        <EvocatDisplay @evocat-created="evocatCreated" />
        <div v-if="evocat">
            <div 
                v-if="!initializeType"
                class="button-row"
            >
                <button
                    @click="setInitializeType('load')"
                >
                    Load Initial Mapping
                </button>
                <button
                    @click="setInitializeType('create')"
                >
                    Create New Mapping
                </button>
            </div>
            <AccessPathLoader
                v-if="initializeType === 'load'"
                :selected-logical-model="selectedLogicalModel"
                @finish="createMapping"
            />
            <AccessPathCreator2
                v-if="initializeType === 'create'"
                :selected-logical-model="selectedLogicalModel"
                @finish="createMapping"
            />
        </div>
    </div>
</template>

<style>
.button-row {
    display: flex;
    gap: 10px;
    justify-content: center;
}
</style>
