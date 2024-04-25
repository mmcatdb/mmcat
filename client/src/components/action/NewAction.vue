<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import API from '@/utils/api';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';
import type { Datasource } from '@/types/datasource';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { type ActionPayloadInit, ACTION_TYPES, Action } from '@/types/action';

const emit = defineEmits<{
    (e: 'newAction', action: Action): void;
}>();

const logicalModels = ref<LogicalModel[]>();
const datasources = ref<Datasource[]>();
const fetched = ref(false);
const logicalModelId = ref<Id>();
const actionName = ref<string>('');
const actionType = ref(ACTION_TYPES[0].value);
const fetching = ref(false);

const categoryId = useSchemaCategoryId();

onMounted(async () => {
    const logicalModelResult = await API.logicalModels.getAllLogicalModelsInCategory({ categoryId });
    if (logicalModelResult.status)
        logicalModels.value = logicalModelResult.data.map(LogicalModel.fromServer);

    const datasourceResult = await API.datasources.getAllDatasources({});
    if (datasourceResult.status)
        datasources.value = datasourceResult.data;

    fetched.value = true;
});

const dataValid = computed(() => {
    if (!actionName.value)
        return false;

    return !!logicalModelId.value;
});

async function createAction() {
    fetching.value = true;

    const payload = {
        type: actionType.value,
        logicalModelId: logicalModelId.value,
    };

    const result = await API.actions.createAction({}, {
        categoryId,
        label: actionName.value,
        payload: payload as ActionPayloadInit,
    });
    if (result.status)
        emit('newAction', Action.fromServer(result.data));

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
            <ValueRow label="Logical model:">
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
