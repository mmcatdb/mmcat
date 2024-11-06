<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import API from '@/utils/api';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';
import { Datasource } from '@/types/datasource';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { type ActionPayloadInit, ACTION_TYPES, Action, ActionType } from '@/types/action';

const emit = defineEmits<{
    (e: 'newAction', action: Action): void;
}>();

const datasources = ref<Datasource[]>();
const fetched = ref(false);
const datasourceId = ref<Id>();
const datasourceIds = ref<Id[]>([]);
const selectedDatasources = computed(() => {
    const all = datasources.value;
    return all ? datasourceIds.value.map(id => all.find(ds => ds.id === id)!) : [];
});
const notSelectedDatasources = computed(() => {
    const all = datasources.value;
    return all ? all.filter(ds => !datasourceIds.value.includes(ds.id)) : [];
});
const actionName = ref<string>('');
type AvailableOptions = typeof ACTION_TYPES[number]['value'];
const actionType = ref<AvailableOptions>(ACTION_TYPES[0].value);
const fetching = ref(false);

const categoryId = useSchemaCategoryId();

onMounted(async () => {
    const datasourceResult = await API.datasources.getAllDatasources({});
    if (datasourceResult.status)
        datasources.value = datasourceResult.data.map(Datasource.fromServer);

    fetched.value = true;
});

const dataValid = computed(() => {
    if (!actionName.value)
        return false;

    return actionType.value === ActionType.RSDToCategory ? !!datasourceIds.value.length : !!datasourceId.value;
});

async function createAction() {
    fetching.value = true;
    const payload: ActionPayloadInit = actionType.value === ActionType.RSDToCategory ? {
        type: actionType.value,
        datasourceIds: datasourceIds.value,
    } : {
        type: actionType.value,
        datasourceId: datasourceId.value as Id,
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

function addDatasource() {
    if (datasourceId.value && !datasourceIds.value.includes(datasourceId.value)) {
        datasourceIds.value.unshift(datasourceId.value);
        datasourceId.value = undefined;
    }
}

function removeDatasource(id: Id) {
    datasourceIds.value = datasourceIds.value.filter(dsId => dsId !== id);
}
</script>

<template>
    <div class="newAction">
        <h2>Create a new action</h2>
        <ValueContainer>
            <ValueRow label="Type:">
                <select
                    v-model="actionType"
                    class="w-100"
                >
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
                <input
                    v-model="actionName"
                    class="w-100"
                />
            </ValueRow>
            <ValueRow
                v-if="actionType === ActionType.RSDToCategory"
                label="Datasources:"
            >
                <div class="d-flex align-items-center gap-2">
                    <select
                        v-model="datasourceId"
                        style="width: 200px;"
                    >
                        <option
                            v-for="datasource in notSelectedDatasources"
                            :key="datasource.id"
                            :value="datasource.id"
                        >
                            {{ datasource.label }}
                        </option>
                    </select>
                    <button @click="addDatasource">
                        Add
                    </button>
                </div>
                <div
                    v-for="datasource in selectedDatasources"
                    :key="datasource.id"
                    class="d-flex align-items-center gap-2 py-1"
                >
                    <button @click="removeDatasource(datasource.id)">
                        x
                    </button>
                    {{ datasource.label }}
                </div>
            </ValueRow>
            <ValueRow
                v-else
                label="Datasource:"
            >
                <select v-model="datasourceId">
                    <option
                        v-for="datasource in datasources"
                        :key="datasource.id"
                        :value="datasource.id"
                    >
                        {{ datasource.label }}
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
