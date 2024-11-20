<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import API from '@/utils/api';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';
import { Datasource } from '@/types/datasource';
import { type JobPayloadInit, ACTION_TYPES, Action, ActionType } from '@/types/action';
import { Mapping } from '@/types/mapping';
import type { ActionStepInnerValue } from './NewActionStep.vue';
import NewActionStep from './NewActionStep.vue';

const emit = defineEmits<{
    (e: 'newAction', action: Action): void;
}>();

const datasources = ref<Datasource[]>();
const mappings = ref<Mapping[]>();

const actionName = ref<string>('');
type AvailableTypes = typeof ACTION_TYPES[number]['value'];
const actionType = ref<AvailableTypes>(ACTION_TYPES[0].value);

const steps = ref<ActionStepInnerValue[]>([ {
    datasourceId: undefined,
    datasourceIds: [],
    mappingIds: [],
} ]);

function addStep() {
    steps.value.push({
        datasourceId: undefined,
        datasourceIds: [],
        mappingIds: [],
    });
}

function removeStep(index: number) {
    steps.value.splice(index, 1);
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

const isDataValid = computed(() => {
    if (!actionName.value)
        return false;

    return actionType.value === ActionType.RSDToCategory
        ? steps.value.every(step => !!step.datasourceIds.length)
        : steps.value.every(step => !!step.datasourceId);
});

async function createAction() {
    fetching.value = true;
    const payloads: JobPayloadInit[] = steps.value.map(step => {
        return actionType.value === ActionType.RSDToCategory
            ? {
                type: actionType.value,
                datasourceIds: step.datasourceIds,
            } : {
                type: actionType.value,
                datasourceId: step.datasourceId as Id,
                mappingIds: step.mappingIds,
            };
    });

    const result = await API.actions.createAction({}, {
        categoryId,
        label: actionName.value,
        payloads,
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
            <div class="row align-items-center gx-1 mt-2">
                <span class="col-3 text-end">Label:</span>
                <div class="col">
                    <input
                        v-model="actionName"
                        class="w-100"
                    />
                </div>
            </div>
            <template v-if="mappings && datasources">
                <div class="row gx-1 mt-2">
                    <div class="col-3 text-end">
                        Steps:
                    </div>
                </div>
                <div class="d-flex flex-column gap-3">
                    <div
                        v-for="(_, index) in steps"
                        :key="index"
                        class="row gx-1"
                    >
                        <div class="col-3 d-flex align-items-center justify-content-center border-end border-2 my-1">
                            <button
                                :disabled="steps.length === 1"
                                @click="removeStep(index)"
                            >
                                x
                            </button>
                        </div>
                        <NewActionStep
                            v-model="steps[index]"
                            class="col"
                            :type="actionType"
                            :datasources="datasources"
                            :mappings="mappings"
                        />
                    </div>
                </div>
            </template>
        </div>
        <div class="button-row mt-4">
            <button
                :disabled="(fetching || !isDataValid)"
                @click="createAction"
            >
                Create action
            </button>
            <button @click="addStep">
                Add step
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
    width: 500px;
}
</style>
