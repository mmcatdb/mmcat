<script setup lang="ts">
import { computed, onMounted, provide, ref, shallowRef } from 'vue';
import { GraphRootProperty, GraphSimpleProperty, GraphComplexProperty } from '@/types/accessPath/graph';
import type { GraphChildProperty, GraphParentProperty } from '@/types/accessPath/graph/compositeTypes';
import { SignatureId, StaticName } from '@/types/identifiers';
import { type Node, type Graph, SelectionType } from '@/types/categoryGraph';
import AccessPathEditor2 from './edit/AccessPathEditor2.vue';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryInfo, useSchemaCategoryId, evocatKey, type EvocatContext, useEvocat } from '@/utils/injects';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SingleNodeInput from '@/components/input/SingleNodeInput.vue';
import NodeInput from '@/components/input/NodeInput.vue';
import { isKeyPressed, Key } from '@/utils/keyboardInput';
import API from '@/utils/api';
import { Mapping } from '@/types/mapping';

const { graph } = $(useEvocat());

const props = defineProps<{
    selectedLogicalModel: LogicalModel;
}>();

const accessPath = ref<GraphRootProperty>();
const originalMapping = ref<Mapping>();
const mappingConfirmed = ref(false);

const emit = defineEmits([ 'finish', 'cancel' ]);

onMounted(async () => {
    const logicalModelId = props.selectedLogicalModel.id;
    const result = await API.mappings.getAllMappingsInLogicalModel({ logicalModelId });
    if (result.status) {
        originalMapping.value = Mapping.fromServer(result.data[0]);
        // highlight it in the editor
    }
});

function confirmMapping() {
    mappingConfirmed.value = true;
    if (originalMapping.value)
        accessPath.value = originalMapping.value?.accessPath
}

function updateRootProperty(newRootProperty: GraphRootProperty) {
    accessPath.value?.node.removeRoot();
    accessPath.value?.unhighlightPath();

    newRootProperty.node.becomeRoot();
    accessPath.value = newRootProperty;
    accessPath.value.highlightPath();
}

function createMapping(primaryKey: SignatureId) {
    emit('finish', primaryKey, accessPath);
}

function cancel() {
    emit('cancel');
}

</script>

<template>
    <div class="divide">
        <div>
            <div
                v-if="!accessPath || !selectedLogicalModel"
                class="loader"
            >
                <div v-if="!mappingConfirmed" class="button-row">
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
                v-else
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
