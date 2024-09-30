<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { GraphRootProperty } from '@/types/accessPath/graph';
import { SignatureId } from '@/types/identifiers';
import AccessPathEditor2 from './edit/AccessPathEditor2.vue';
import { LogicalModel } from '@/types/logicalModel';
import { useEvocat } from '@/utils/injects';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import API from '@/utils/api';
import { Mapping } from '@/types/mapping';

const { graph } = $(useEvocat());

const props = defineProps<{
    logicalModels: LogicalModel[];
}>();

const accessPath = ref<GraphRootProperty>();
const originalMapping = ref<Mapping>();
const originalGraphProperty = ref<GraphRootProperty>();
const selectedLogicalModel = ref<LogicalModel>();
const selectedMapping = ref<Mapping>();
const mappings = ref<Mapping[]>([]);

const emit = defineEmits([ 'finish', 'cancel' ]);

onMounted(async () => {
    await loadMappingsForSelectedLogicalModel();
});

watch(selectedLogicalModel, async (newModel) => {
    if (newModel) 
        await loadMappingsForSelectedLogicalModel();
});

watch(selectedMapping, (newMapping) => {
    if (newMapping) 
        loadSelectedMapping(newMapping); 
});

async function loadMappingsForSelectedLogicalModel() {
    if (!selectedLogicalModel.value) return;

    const logicalModelId = selectedLogicalModel.value.id;
    const result = await API.mappings.getAllMappingsInLogicalModel({ logicalModelId });
    if (result.status) 
        mappings.value = result.data.map(Mapping.fromServer);
}

async function loadSelectedMapping(mapping: Mapping) {
    originalMapping.value = mapping;
    const node = graph.getNode(mapping.rootObjectKey) || null;

    originalGraphProperty.value = GraphRootProperty.fromRootProperty(
        mapping.accessPath, 
        node,
    );
    originalGraphProperty.value?.highlightPath();
}

function confirmMapping() {
    if (originalMapping.value)
        accessPath.value = originalGraphProperty.value;
}

function updateRootProperty(newRootProperty: GraphRootProperty) {
    accessPath.value?.node.removeRoot();
    accessPath.value?.unhighlightPath();

    newRootProperty.node.becomeRoot();
    accessPath.value = newRootProperty;
    accessPath.value.highlightPath();
}

function undoAccessPath() {
    originalGraphProperty.value?.unhighlightPath();
    accessPath.value?.node.removeRoot();
    accessPath.value?.unhighlightPath();
}

function createMapping(primaryKey: SignatureId) {
    emit('finish', primaryKey, accessPath);
}

function cancel() {
    undoAccessPath();
    emit('cancel');
}

</script>

<template>
    <div class="divide">
        <div>
            <div v-if="props.logicalModels.length">
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
            </div>
            <div v-if="props.logicalModels.length">
                <ValueRow label="Kind:">
                    <select v-model="selectedMapping">
                        <option 
                            v-for="mapping in mappings" 
                            :key="mapping.id" 
                            :value="mapping"
                        >
                            {{ mapping.kindName }}
                        </option>
                    </select>
                </ValueRow>
            </div>
            <div
                v-if="!accessPath"
                class="loader"
            >
                <div class="button-row">
                    <button
                        @click="confirmMapping"
                    >
                        Confirm Initial Mapping
                    </button>
                    <button
                        @click="cancel"
                    >
                        Cancel
                    </button>
                </div>
            </div>
            <AccessPathEditor2
                v-else-if="selectedLogicalModel"
                :datasource="selectedLogicalModel.datasource"
                :root-property="accessPath"
                @finish="createMapping"
                @update:rootProperty="updateRootProperty"
            />
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

.loader {
    display: flex;
    flex-direction: column;
}

.display {
    padding: 16px;
    margin: 16px;
}

</style>
