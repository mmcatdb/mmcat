<script setup lang="ts">
import { onMounted, provide, ref, shallowRef } from 'vue';
import type { GraphRootProperty } from '@/types/accessPath/graph';
import { SignatureId } from '@/types/identifiers';
import type { Graph } from '@/types/categoryGraph';
import { useSchemaCategoryId, evocatKey, type EvocatContext } from '@/utils/injects';
import API from '@/utils/api';
import { useRoute } from 'vue-router';
import type { Evocat } from '@/types/evocat/Evocat';
import EvocatDisplay from '../category/EvocatDisplay.vue';
import AccessPathLoader from './AccessPathLoader.vue';
import AccessPathCreator from '@/components/accessPath/AccessPathCreator.vue';
import { useFixedRouter } from '@/router/specificRoutes';
import { Datasource } from '@/types/datasource';
import { Mapping } from '@/types/mapping';

/**
 * Vue router instances for navigating and accessing route parameters.
 */
const route = useRoute();
const router = useFixedRouter();

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
 * @param context - The Evocat and graph context.
 * @param context.evocat - The created Evocat instance.
 * @param context.graph - The created Graph instance.
 */
function evocatCreated(context: { evocat: Evocat, graph: Graph }) {
    evocat.value = context.evocat;
    graph.value = context.graph;
}

/**
 * Stores the list of datasources and the selected datasource.
 */
const datasources = shallowRef<Datasource[]>([]);
const selectedDatasource = shallowRef<Datasource>();

/**
 * Retrieves the schema category ID and category information.
 */
const categoryId = useSchemaCategoryId();

/**
 * Tracks the current initialization type ('create', 'load', or 'default').
 */
const initializeType = ref<'create' | 'load' | 'default'>('default');

/**
 * Fetches all datasources in the selected category and sets the selected datasource based on the route query parameter.
 */
onMounted(async () => {
    const result = await API.datasources.getAllDatasources({});
    if (result.status) {
        datasources.value = result.data.map(Datasource.fromServer);
        selectedDatasource.value = datasources.value.find(model => model.id.toString() === route.query.datasourceId);
    }
});

/**
 * Creates a new mapping with the provided primary key, access path, and kind name.
 * @param primaryKey - The primary key of the mapping.
 * @param accessPath - The root property of the access path.
 * @param kindName - The name of the kind being created (optional).
 */
async function createMapping(primaryKey: SignatureId, accessPath: GraphRootProperty, kindName: string) {
    if (!selectedDatasource.value || !graph.value)
        return;

    const result = await API.mappings.createMapping({}, {
        categoryId,
        datasourceId: selectedDatasource.value.id,
        rootObjectKey: accessPath.node.schemaObjex.key.toServer(),
        primaryKey: new SignatureId(primaryKey.signatures).toServer(),
        kindName,
        accessPath: accessPath.toServer(),
    });
    if (result.status) {
        const mapping = Mapping.fromServer(result.data);
        router.push({ name: 'datasource', params: { id: mapping.datasourceId } });
    }
}

/**
 * Sets the initialization type to either 'create' or 'load'.
 * @param type - The type of initialization to set.
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
                <h5 style="text-align: center;">
                    Select Mapping Initialization
                </h5>
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
                :datasources="datasources"
                @finish="createMapping"
                @cancel="setToDefault"
            />
            <AccessPathCreator
                v-if="initializeType === 'create' && selectedDatasource"
                :selected-datasource="selectedDatasource"
                @finish="createMapping"
                @cancel="setToDefault"
            />
        </div>
    </div>
</template>

<style>
.editor {
    display: flex;
    flex-direction: column;
}
</style>
