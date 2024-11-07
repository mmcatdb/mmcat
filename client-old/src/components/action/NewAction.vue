<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import API from '@/utils/api';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';
import { Datasource } from '@/types/datasource';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { type ActionPayloadInit, ACTION_TYPES, Action, ActionType } from '@/types/action';
import { Mapping } from '@/types/mapping';

const emit = defineEmits<{
    (e: 'newAction', action: Action): void;
}>();

const datasources = ref<Datasource[]>();
const mappings = ref<Mapping[]>();

// Common

const actionName = ref<string>('');
type AvailableTypes = typeof ACTION_TYPES[number]['value'];
const actionType = ref<AvailableTypes>(ACTION_TYPES[0].value);
const datasourceId = ref<Id>();

// For RSDToCategory

const datasourceIds = ref<Id[]>([]);
const selectedDatasources = computed(() => {
    const all = datasources.value;
    return all ? datasourceIds.value.map(id => all.find(ds => ds.id === id)!) : [];
});
const availableDatasources = computed(() => {
    return datasources.value?.filter(ds => !datasourceIds.value.includes(ds.id)) ?? [];
});

function addDatasource() {
    if (datasourceId.value && !datasourceIds.value.includes(datasourceId.value)) {
        datasourceIds.value.unshift(datasourceId.value);
        datasourceId.value = undefined;
    }
}

function removeDatasource(id: Id) {
    datasourceIds.value = datasourceIds.value.filter(dsId => dsId !== id);
}

// For ModelToCategory and CategoryToModel

const mappingIds = ref<Id[]>([]);
const selectedMappings = computed(() => {
    const all = mappings.value;
    if (!all)
        return [];

    return mappingIds.value
        .map(id => all.find(m => m.id === id)!)
        .filter(m => m.datasourceId === datasourceId.value);
});
const availableMappings = computed(() => {
    return mappings.value
        ?.filter(m => m.datasourceId === datasourceId.value)
        .filter(m => !mappingIds.value.includes(m.id)) ?? [];
});

function toggleMapping(id: Id) {
    if (mappingIds.value.includes(id))
        mappingIds.value = mappingIds.value.filter(mId => mId !== id);
    else
        mappingIds.value.push(id);
}

onMounted(async () => {
    const results = await Promise.all([
        API.datasources.getAllDatasources({}),
        API.mappings.getAllMappingsInCategory({}, { categoryId }),
    ]);

    if (results[0].status)
        datasources.value = results[0].data.map(Datasource.fromServer);
    if (results[1].status)
        mappings.value = results[1].data.map(Mapping.fromServer);
});

const fetching = ref(false);
const categoryId = useSchemaCategoryId();

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
        mappingIds: selectedMappings.value.length === 0 ? undefined : selectedMappings.value.map(m => m.id),
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
        <div class="d-flex flex-column">
            <div class="row align-items-center gx-1">
                <span class="col-3 text-end">Type:</span>
                <div class="col">
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
                </div>
            </div>
            <div class="row align-items-center gx-1">
                <span class="col-3 text-end">Label:</span>
                <div class="col">
                    <input
                        v-model="actionName"
                        class="w-100"
                    />
                </div>
            </div>
            <div
                v-if="actionType === ActionType.RSDToCategory"
                class="row align-items-center gx-1"
            >
                <span class="col-3 text-end">Datasources:</span>
                <div class="col">
                    <div class="d-flex align-items-center gap-2">
                        <select
                            v-model="datasourceId"
                            class="flex-grow-1"
                        >
                            <option
                                v-for="datasource in availableDatasources"
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
                </div>
                <div class="row gx-1">
                    <div class="col-3" />
                    <div class="col">
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
                    </div>
                </div>
            </div>
            <template v-else>
                <div class="row align-items-center gx-1">
                    <span class="col-3 text-end">Datasource:</span>
                    <div class="col">
                        <select
                            v-model="datasourceId"
                            class="w-100"
                        >
                            <option
                                v-for="datasource in datasources"
                                :key="datasource.id"
                                :value="datasource.id"
                            >
                                {{ datasource.label }}
                            </option>
                        </select>
                    </div>
                </div>
                <div class="row gx-1">
                    <span class="col-3 text-end">Mappings:</span>
                    <div class="col">
                        <div
                            v-if="selectedMappings.length > 0"
                            class="d-flex flex-wrap gap-1"
                        >
                            <button
                                v-for="mapping in selectedMappings"
                                :key="mapping.id"
                                @click="toggleMapping(mapping.id)"
                            >
                                {{ mapping.kindName }}
                            </button>
                        </div>
                        <div
                            v-else
                            class="fw-bold"
                        >
                            All
                        </div>
                    </div>
                    <div
                        v-if="availableMappings.length > 0"
                        class="row"
                    >
                        <div class="col-3" />
                        <div class="col">
                            <div class="mt-2">
                                Available:
                            </div>
                            <div
                                class="d-flex flex-wrap gap-1"
                            >
                                <button
                                    v-for="mapping in availableMappings"
                                    :key="mapping.id"
                                    @click="toggleMapping(mapping.id)"
                                >
                                    {{ mapping.kindName }}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </div>
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
    width: 420px;
}
</style>
