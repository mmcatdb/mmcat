<script setup lang="ts">
import { onMounted, provide, ref, shallowRef } from 'vue';
import { GraphRootProperty } from '@/types/accessPath/graph';
import { SignatureId } from '@/types/identifiers';
import { type Graph } from '@/types/categoryGraph';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryInfo, useSchemaCategoryId, evocatKey, type EvocatContext } from '@/utils/injects';
import API from '@/utils/api';
import { useRoute, useRouter } from 'vue-router';
import type { Evocat } from '@/types/evocat/Evocat';
import EvocatDisplay from '../category/EvocatDisplay.vue';
import AccessPathLoader from './AccessPathLoader.vue';
import AccessPathCreator from '@/components/accessPath/AccessPathCreator.vue';

/**
 * Vue router instances for navigating and accessing route parameters.
 */
const route = useRoute();
const router = useRouter();

/**
 * Stores references for Evocat and graph instances.
 */
const evocat = shallowRef<Evocat>();
const graph = shallowRef<Graph>();

/**
 * Provides the Evocat context, containing evocat and graph references.
 */
provide(evocatKey, { evocat, graph } as EvocatContext);

/**
 * Handles the creation of Evocat and graph instances when emitted by EvocatDisplay.
 * @param {Object} context - The Evocat and graph context.
 * @param {Evocat} context.evocat - The created Evocat instance.
 * @param {Graph} context.graph - The created Graph instance.
 */
function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    graph.value = context.graph;
}

/**
 * Stores the list of logical models and the selected logical model.
 */
const logicalModels = shallowRef<LogicalModel[]>([]);
const selectedLogicalModel = shallowRef<LogicalModel>();

/**
 * Retrieves the schema category ID and category information.
 */
const categoryId = useSchemaCategoryId();
const category = useSchemaCategoryInfo();

/**
 * Tracks the current initialization type ('create', 'load', or 'default').
 */
const initializeType = ref<'create' | 'load' | 'default'>('default');

/**
 * Fetches all logical models in the selected category and sets the selected logical model based on the route query parameter.
 */
onMounted(async () => {
    const result = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    if (result.status) {
        logicalModels.value = result.data.map(LogicalModel.fromServer);
        selectedLogicalModel.value = logicalModels.value.find(model => model.id.toString() === route.query.logicalModelId);
    }
});

/**
 * Creates a new mapping with the provided primary key, access path, and kind name.
 * @param {SignatureId} primaryKey - The primary key of the mapping.
 * @param {GraphRootProperty} accessPath - The root property of the access path.
 * @param {string | undefined} kindName - The name of the kind being created (optional).
 */
async function createMapping(primaryKey: SignatureId, accessPath: GraphRootProperty, kindName: string | undefined) {
    if (!selectedLogicalModel.value || !graph.value || !accessPath)
        return;
    accessPath.node.schemaObject;

    const newKindName = kindName !== undefined ? kindName : accessPath.name.toString();

    const result = await API.mappings.createNewMapping({}, {
        logicalModelId: selectedLogicalModel.value.id,
        rootObjectKey: accessPath.node.schemaObject.key.toServer(),
        primaryKey: new SignatureId(selectedLogicalModel.value.datasource.configuration.isSchemaless ? [] : primaryKey.signatures).toServer(),
        kindName: newKindName,
        accessPath: accessPath.toServer(),
        categoryVersionn: category.value.versionId,
    });
    if (result.status)
        router.push({ name: 'logicalModel', params: { id: selectedLogicalModel.value.id } });
}

/**
 * Sets the initialization type to either 'create' or 'load'.
 * @param {'create' | 'load'} type - The type of initialization to set.
 */
function setInitializeType(type: 'create' | 'load') {
    initializeType.value = type;
}

/**
 * Resets the initialization type to 'default'.
 */
function setToDefault() {
    initializeType.value = 'default';
}

</script>

<template>
    <div class="divide">
        <EvocatDisplay @evocat-created="evocatCreated" />
        <div v-if="evocat">
            <div
                v-if="initializeType === 'default'" 
                class="editor"
            >
                <h5>Select Mapping initialization:</h5>
                <div 
                    class="button-row"
                >
                    <button
                        @click="setInitializeType('load')"
                    >
                        Load Initial
                    </button>
                    <button
                        @click="setInitializeType('create')"
                    >
                        Create New
                    </button>
                </div>
            </div>
            <AccessPathLoader
                v-if="initializeType === 'load'"
                :logical-models="logicalModels"
                @finish="createMapping"
                @cancel="setToDefault"
            />
            <AccessPathCreator
                v-if="initializeType === 'create' && selectedLogicalModel"
                :selected-logical-model="selectedLogicalModel"
                @finish="createMapping"
                @cancel="setToDefault"
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

.editor {
    display: flex;
    flex-direction: column;
}
</style>
