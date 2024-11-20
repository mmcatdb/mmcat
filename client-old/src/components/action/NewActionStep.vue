<script setup lang="ts">
import { computed, shallowRef, watch } from 'vue';
import type { Id } from '@/types/id';
import { Datasource } from '@/types/datasource';
import { ActionType } from '@/types/action';
import { Mapping } from '@/types/mapping';

export type ActionStepInnerValue = {
    datasourceId?: Id;
    datasourceIds: Id[];
    mappingIds: Id[];
};

const props = defineProps<{
    type: ActionType;
    modelValue: ActionStepInnerValue;
    datasources: Datasource[];
    mappings: Mapping[];
}>();

const emit = defineEmits([ 'update:modelValue', 'delete' ]);

const innerValue = shallowRef(props.modelValue);

function updateInnerValue(newValue: ActionStepInnerValue): void {
    innerValue.value = newValue;
    emit('update:modelValue', innerValue.value);
}

watch(() => props.modelValue, (newValue: ActionStepInnerValue) => {
    innerValue.value = newValue;
});

// Common

function updateDatasource(event: Event): void {
    updateInnerValue({ ...innerValue.value, datasourceId: (event.target as unknown as { value?: string }).value });
}

// For RSDToCategory

const selectedDatasources = computed(() => {
    const all = props.datasources;
    return all ? innerValue.value.datasourceIds.map(id => all.find(ds => ds.id === id)!) : [];
});
const availableDatasources = computed(() => {
    return props.datasources.filter(ds => !innerValue.value.datasourceIds.includes(ds.id)) ?? [];
});

function addDatasource() {
    if (!innerValue.value.datasourceId)
        return;

    updateInnerValue({
        ...innerValue.value,
        datasourceIds: [ innerValue.value.datasourceId, ...innerValue.value.datasourceIds ],
    });
}

function removeDatasource(id: Id) {
    updateInnerValue({
        ...innerValue.value,
        datasourceIds: innerValue.value.datasourceIds.filter(dsId => dsId !== id),
    });
}

// For ModelToCategory and CategoryToModel

const selectedMappings = computed(() => {
    const all = props.mappings;
    if (!all)
        return [];

    return innerValue.value.mappingIds
        .map(id => all.find(m => m.id === id)!)
        .filter(m => m.datasourceId === innerValue.value.datasourceId);
});
const availableMappings = computed(() => {
    return props.mappings
        .filter(m => m.datasourceId === innerValue.value.datasourceId)
        .filter(m => !innerValue.value.mappingIds.includes(m.id)) ?? [];
});

function toggleMapping(id: Id) {
    const mappingIds = innerValue.value.mappingIds;
    const newMappingIds = mappingIds.includes(id)
        ? mappingIds.filter(mId => mId !== id)
        : [ ...mappingIds, id ];

    updateInnerValue({ ...innerValue.value, mappingIds: newMappingIds });
}
</script>

<template>
    <div>
        <div class="d-flex flex-column">
            <div
                v-if="type === ActionType.RSDToCategory"
                class="row align-items-center gx-1"
            >
                <span class="col-3 text-end">Datasources:</span>
                <div class="col">
                    <div class="d-flex align-items-center gap-2">
                        <select
                            :value="innerValue.datasourceId"
                            class="flex-grow-1"
                            @input="updateDatasource"
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
                            :value="innerValue.datasourceId"
                            class="w-100"
                            @input="updateDatasource"
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
                <div class="row gx-1 mt-2">
                    <span class="col-3 text-end lh-button">Mappings:</span>
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
                            class="fw-bold lh-button px-3"
                        >
                            All
                        </div>
                    </div>
                </div>
                <div
                    v-if="availableMappings.length > 0"
                    class="row gx-1 mt-2"
                >
                    <span class="col-3 text-end lh-button">Available:</span>
                    <div class="col d-flex flex-wrap gap-1">
                        <button
                            v-for="mapping in availableMappings"
                            :key="mapping.id"
                            @click="toggleMapping(mapping.id)"
                        >
                            {{ mapping.kindName }}
                        </button>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<style>
.lh-button {
    line-height: 32px;
}
</style>