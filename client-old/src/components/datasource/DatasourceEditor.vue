<script setup lang="ts">
import { Datasource, DATASOURCE_TYPES, DatasourceType, validateSettings, type Settings, isDatabase, isFile, type DatasourceInit } from '@/types/datasource';
import API from '@/utils/api';
import { computed, ref, watch } from 'vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { useRoute } from 'vue-router';
import { tryUseWorkflow } from '@/utils/injects';
import { type WorkflowData } from '@/types/workflow';

const props = defineProps<{
    datasource?: Datasource;
}>();

const emit = defineEmits<{
    (e: 'save', datasource: Datasource): void;
    (e: 'cancel'): void;
    (e: 'delete'): void;
}>();

const fetching = ref(false);

type InnerValue = {
    type?: DatasourceType;
    label: string;
    settings: Settings;
};

const initialValue: InnerValue = props.datasource
    ? { ...props.datasource, settings: { ...props.datasource.settings } }
    : { label: '', settings: {} };

const innerValue = ref<InnerValue>(initialValue);

const isNew = computed(() => !props.datasource);
const isValid = computed(() => {
    if (!innerValue.value.label)
        return false;

    const type = innerValue.value.type ?? props.datasource?.type;
    if (!type)
        return false;

    return validateSettings(innerValue.value.settings, type);
});

const showDatabaseOptions = computed(() => innerValue.value.type && isDatabase(innerValue.value.type));
const showFileOptions = computed(() => innerValue.value.type && isFile(innerValue.value.type));

watch(() => innerValue.value.type, (type) => {
    if (type && isDatabase(type)) {
        innerValue.value.settings.isWritable = true;
        innerValue.value.settings.isQueryable = true;
    }
    else {
        innerValue.value.settings.isWritable = false;
        innerValue.value.settings.isQueryable = false;
    }
});

async function save() {
    fetching.value = true;

    await (isNew.value ? createNew() : updateOld());

    fetching.value = false;
}

// If we are in the phase of creating and selecting a new datasource as input for the workflow, we need to set it on the workflow.
const route = useRoute();
const isSelecting = ref(route.query.state === 'selecting');
const workflow = tryUseWorkflow();

async function createNew() {
    const init = innerValue.value;
    if (!init.type)
        return;

    const result = await API.datasources.createDatasource({}, init as DatasourceInit);
    if (!result.status)
        return;

    const newDatasource = Datasource.fromServer(result.data);

    if (workflow && isSelecting.value) {
        const newData: WorkflowData = { ...workflow.value.data, inputDatasourceId: newDatasource.id };
        const result = await API.workflows.updateWorkflowData({ id: workflow.value.id }, newData);
        if (!result.status)
            return;

        workflow.value = result.data;
    }

    emit('save', newDatasource);
}

async function updateOld() {
    if (!props.datasource)
        return;

    if (innerValue.value.settings.password === '')
        innerValue.value.settings.password = undefined;

    const result = await API.datasources.updateDatasource({ id: props.datasource.id }, innerValue.value);
    if (result.status)
        emit('save', result.data);
}

function cancel() {
    emit('cancel');
}

async function deleteMethod() {
    if (!props.datasource)
        return;

    fetching.value = true;
    const result = await API.datasources.deleteDatasource({ id: props.datasource.id });
    if (result.status)
        emit('delete');

    fetching.value = false;
}
</script>

<template>
    <div class="editor">
        <h2>{{ isNew ? 'Add' : 'Edit' }} Datasource</h2>
        <ValueContainer>
            <ValueRow label="Type:">
                <select
                    v-model="innerValue.type"
                    :disabled="!isNew"
                >
                    <option
                        v-for="availableType in DATASOURCE_TYPES"
                        :key="availableType.type"
                        :value="availableType.type"
                    >
                        {{ availableType.label }}
                    </option>
                </select>
            </ValueRow>
            <ValueRow label="Label:">
                <input v-model="innerValue.label" />
            </ValueRow>
            <ValueRow
                v-if="showFileOptions"
                label="URL:"
            >
                <input v-model="innerValue.settings.url" />
            </ValueRow>
            <ValueRow
                v-if="showDatabaseOptions"
                label="Host:"
            >
                <input v-model="innerValue.settings.host" />
            </ValueRow>
            <ValueRow
                v-if="showDatabaseOptions"
                label="Port:"
            >
                <input
                    v-model="innerValue.settings.port"
                    type="number"
                />
            </ValueRow>
            <ValueRow
                v-if="showDatabaseOptions"
                label="Database:"
            >
                <input v-model="innerValue.settings.database" />
            </ValueRow>
            <ValueRow
                :class="{ hidden: innerValue.type !== DatasourceType.mongodb }"
                label="Authentication database:"
            >
                <input v-model="innerValue.settings.authenticationDatabase" />
            </ValueRow>
            <ValueRow
                v-if="showDatabaseOptions"
                label="Username:"
            >
                <input v-model="innerValue.settings.username" />
            </ValueRow>
            <ValueRow
                v-if="showDatabaseOptions"
                label="Password:"
            >
                <input v-model="innerValue.settings.password" />
            </ValueRow>
            <ValueRow
                label="Is writable:"
            >
                <input
                    v-model="innerValue.settings.isWritable"
                    type="checkbox"
                />
            </ValueRow>
            <ValueRow
                label="Is queryable:"
            >
                <input
                    v-model="innerValue.settings.isQueryable"
                    type="checkbox"
                />
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="fetching || !isValid"
                @click="save"
            >
                {{ isNew ? 'Add' : 'Save' }}
            </button>
            <button
                :disabled="fetching"
                @click="cancel"
            >
                Cancel
            </button>
            <button
                v-if="!isNew"
                :disabled="fetching"
                @click="deleteMethod"
            >
                Delete
            </button>
        </div>
    </div>
</template>
