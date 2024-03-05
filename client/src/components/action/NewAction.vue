<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import API from '@/utils/api';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';
import { DataSource } from '@/types/dataSource';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { ActionType, type ActionPayloadInit, ACTION_TYPES, Action } from '@/types/action';

const emit = defineEmits<{
    (e: 'newAction', action: Action): void;
}>();

const logicalModels = ref<LogicalModel[]>();
const dataSources = ref<DataSource[]>();
const fetched = ref(false);
const logicalModelId = ref<Id>();
const dataSourceId = ref<Id>();
const actionName = ref<string>('');
const actionType = ref(ACTION_TYPES[0].value);
const fetching = ref(false);

const categoryId = useSchemaCategoryId();

onMounted(async () => {
    const logicalModelResult = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    if (logicalModelResult.status)
        logicalModels.value = logicalModelResult.data.map(LogicalModel.fromServer);

    const dataSourceResult = await API.dataSources.getAllDataSources({});
    if (dataSourceResult.status)
        dataSources.value = dataSourceResult.data.map(DataSource.fromServer);

    fetched.value = true;
});

const dataValid = computed(() => {
    if (!actionName.value)
        return false;

    return actionType.value === ActionType.JsonLdToCategory || actionType.value === ActionType.RSDToCategory
        ? !!dataSourceId.value
        : !!logicalModelId.value;
});

async function createAction() {
    fetching.value = true;
    let payload;

    if (actionType.value === ActionType.JsonLdToCategory || actionType.value === ActionType.RSDToCategory) {
        payload = {
            type: actionType.value,
            dataSourceId: dataSourceId.value,
        };
    }  
    else {
        payload = {
            type: actionType.value,
            logicalModelId: logicalModelId.value,
        };
    } 
    /*
    const payload = actionType.value === ActionType.JsonLdToCategory ? {
        type: ActionType.JsonLdToCategory,
        dataSourceId: dataSourceId.value,
    } : {
        type: actionType.value,
        logicalModelId: logicalModelId.value,
    };*/

    const result = await API.actions.createAction({}, {
        categoryId,
        label: actionName.value,
        payload: payload as ActionPayloadInit,
    });
    if (result.status)
        emit('newAction', Action.fromServer(result.data));
    //else {const a = "a"}

    fetching.value = false;
}
</script>

<template>
    <div class="newAction">
        <h2>Create a new action</h2>
        <ValueContainer>
            <ValueRow label="Type:">
                <select v-model="actionType">
                    <option
                        v-for="availableType in ACTION_TYPES"
                        :key="availableType.value"
                        :value="availableType.value"
                    >
                        {{ availableType.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow label="Label:">
                <input v-model="actionName" />
            </ValueRow>
            <ValueRow
                v-if="actionType === ActionType.JsonLdToCategory || actionType === ActionType.RSDToCategory"
                label="Data source:"
            >
                <select v-model="dataSourceId">
                    <option
                        v-for="dataSource in dataSources"
                        :key="dataSource.id"
                        :value="dataSource.id"
                    >
                        {{ dataSource.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow
                v-else
                label="Logical model:"
            >
                <select v-model="logicalModelId">
                    <option
                        v-for="logicalModel in logicalModels"
                        :key="logicalModel.id"
                        :value="logicalModel.id"
                    >
                        {{ logicalModel.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow>&nbsp;</ValueRow><!-- To make the NewAction tile look the same as the ActionDisplay tile. -->
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="(fetching || !dataValid)"
                @click="createAction"
            >
                Create action
            </button>
        </div>
    </div>
</template>

<style scoped>
.newAction {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    min-width: 284px;
}
</style>
