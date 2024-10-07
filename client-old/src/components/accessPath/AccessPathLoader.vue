<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue';
import { GraphRootProperty } from '@/types/accessPath/graph';
import { SignatureId } from '@/types/identifiers';
import AccessPathEditor from './edit/AccessPathEditor.vue';
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
const mappingConfirmed = ref(false);
const isConfirmDisabled = computed(() => !selectedMapping.value);

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
    originalGraphProperty.value?.unhighlightPath();
    originalGraphProperty.value?.node.removeRoot();
    originalMapping.value = mapping;
    const node = graph.getNode(mapping.rootObjectKey) || null;

    originalGraphProperty.value = GraphRootProperty.fromRootProperty(
        mapping.accessPath, 
        node,
    );
    originalGraphProperty.value?.node.becomeRoot();
    originalGraphProperty.value?.highlightPath();
}

function confirmMapping() {
    if (originalMapping.value) {
        accessPath.value = originalGraphProperty.value;
        mappingConfirmed.value = true;
    }
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
    originalGraphProperty.value?.node.removeRoot();
    accessPath.value?.node.removeRoot();
    accessPath.value?.unhighlightPath();
}

function createMapping(primaryKey: SignatureId) {
    emit('finish', primaryKey, accessPath.value, selectedMapping.value?.kindName);
}

function cancel() {
    undoAccessPath();
    mappingConfirmed.value = false;
    emit('cancel');
}

</script>

<template>
    <div class="divide">
        <div>
            <div 
                v-if="props.logicalModels.length && !mappingConfirmed"
                class="editor"
            >
                <ValueRow label="Logical model:">
                    <select 
                        v-model="selectedLogicalModel"
                        :disabled="mappingConfirmed"
                    >
                        <option 
                            v-for="logicalModel in logicalModels" 
                            :key="logicalModel.id" 
                            :value="logicalModel"
                        >
                            {{ logicalModel.label }}
                        </option>
                    </select>
                </ValueRow>
                <ValueRow label="Kind:">
                    <select 
                        v-model="selectedMapping"
                        :disabled="mappingConfirmed"
                    >
                        <option 
                            v-for="mapping in mappings" 
                            :key="mapping.id" 
                            :value="mapping"
                        >
                            {{ mapping.kindName }}
                        </option>
                    </select>
                </ValueRow>         
                <div class="button-row">
                    <button
                        :disabled="isConfirmDisabled"
                        @click="confirmMapping"
                    >
                        Confirm
                    </button>
                    <button
                        @click="cancel"
                    >
                        Cancel
                    </button>
                </div>
            </div>
            <AccessPathEditor
                v-else-if="selectedLogicalModel"
                :datasource="selectedLogicalModel.datasource"
                :root-property="accessPath"
                @finish="createMapping"
                @update:rootProperty="updateRootProperty"
                @cancel="cancel"
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

.display {
    padding: 16px;
    margin: 16px;
}

</style>
