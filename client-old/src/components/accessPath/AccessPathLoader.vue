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
import { Datasource } from '@/types/datasource';

/**
 * Extracts the graph object from Evocat.
 */
const { graph } = $(useEvocat());

/**
 * Props passed to the component.
 * @typedef {Object} Props
 * @property {LogicalModel[]} logicalModels - Array of logical models.
 */
const props = defineProps<{
    datasources: Datasource[];
}>();

/**
 * Reactive references to track the access path, mappings, and selected values.
 */
const accessPath = ref<GraphRootProperty>();
const originalMapping = ref<Mapping>();
const originalGraphProperty = ref<GraphRootProperty>();
const selectedDatasource = ref<Datasource>();
const selectedMapping = ref<Mapping>();
const mappings = ref<Mapping[]>([]);
const mappingConfirmed = ref(false);

/**
 * Computed property that determines if the "Confirm" button should be disabled.
 * @type {import('vue').ComputedRef<boolean>}
 */
const isConfirmDisabled = computed(() => !selectedMapping.value);

/**
 * Emits custom events to the parent component.
 * @emits finish - Emitted when the mapping process is finished.
 * @emits cancel - Emitted when the operation is canceled.
 */
const emit = defineEmits([ 'finish', 'cancel' ]);

/**
 * Lifecycle hook to load mappings for the selected logical model on component mount.
 */
onMounted(async () => {
    await loadMappingsForSelectedDatasource();
});

/**
 * Watches the selected logical model and loads the associated mappings when changed.
 */
watch(selectedDatasource, async (newModel) => {
    if (newModel) 
        await loadMappingsForSelectedDatasource();
});

/**
 * Watches the selected mapping and loads the selected mapping data when changed.
 */
watch(selectedMapping, (newMapping) => {
    if (newMapping) 
        loadSelectedMapping(newMapping); 
});

/**
 * Loads mappings for the currently selected logical model.
 * Fetches the mappings from the server and updates the mappings array.
 */
async function loadMappingsForSelectedDatasource() {
    if (!selectedDatasource.value) return;

    const datasourceId = selectedDatasource.value.id;
    const result = await API.mappings.getAllMappingsInDatasource({ datasourceId });
    if (result.status) 
        mappings.value = result.data.map(Mapping.fromServer);
}

/**
 * Loads the selected mapping and updates the graph property to reflect the mapping's access path.
 * @param {Mapping} mapping - The selected mapping to load.
 */
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

/**
 * Confirms the selected mapping by setting the access path and marking it as confirmed.
 */
function confirmMapping() {
    if (originalMapping.value) {
        accessPath.value = originalGraphProperty.value;
        mappingConfirmed.value = true;
    }
}

/**
 * Updates the root property when modified in the AccessPathEditor.
 * @param {GraphRootProperty} newRootProperty - The new root property to update.
 */
function updateRootProperty(newRootProperty: GraphRootProperty) {
    accessPath.value?.node.removeRoot();
    accessPath.value?.unhighlightPath();

    newRootProperty.node.becomeRoot();
    accessPath.value = newRootProperty;
    accessPath.value.highlightPath();
}

/**
 * Resets the access path and removes any highlights or root node markings.
 */
function undoAccessPath() {
    originalGraphProperty.value?.unhighlightPath();
    originalGraphProperty.value?.node.removeRoot();
    accessPath.value?.node.removeRoot();
    accessPath.value?.unhighlightPath();
}

/**
 * Emits the finish event to create a mapping with the given primary key and selected mapping kind name.
 * @param {SignatureId} primaryKey - The primary key for the mapping.
 */
function createMapping(primaryKey: SignatureId) {
    emit('finish', primaryKey, accessPath.value, selectedMapping.value?.kindName);
}

/**
 * Cancels the current mapping process, resets the access path, and emits the cancel event.
 */
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
                v-if="props.datasources.length && !mappingConfirmed"
                class="editor"
            >
                <ValueRow label="Logical model:">
                    <select 
                        v-model="selectedDatasource"
                        :disabled="mappingConfirmed"
                    >
                        <option 
                            v-for="datasource in datasources" 
                            :key="datasource.id" 
                            :value="datasource"
                        >
                            {{ datasource.label }}
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
                v-else-if="selectedDatasource && accessPath"
                :datasource="selectedDatasource"
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
