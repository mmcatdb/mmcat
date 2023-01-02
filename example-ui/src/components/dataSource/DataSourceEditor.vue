<script setup lang="ts">
import { DataSource, DATA_SOURCE_TYPES, Type, type DataSourceInit, type DataSourceUpdate } from '@/types/dataSource';
import API from '@/utils/api';
import { computed, ref } from 'vue';

interface DataSourceEditorProps {
    dataSource?: DataSource;
}

const props = defineProps<DataSourceEditorProps>();

const emit = defineEmits([ 'save', 'cancel', 'delete' ]);

type DataSourceEdit = {
    url: string;
    label: string;
    type: Type;
}

const fetching = ref(false);
const innerValue = ref<DataSourceEdit>(props.dataSource ? { ...props.dataSource } : {
    url: '',
    label: '',
    type: Type.JsonLdStore
});

function toInit(edit: DataSourceEdit): DataSourceInit | null {
    if (
        !edit.url ||
        !edit.label
    )
        return null;

    return { ...edit };
}

function toUpdate(edit: DataSourceEdit): DataSourceUpdate {
    return {
        url: edit.url ? edit.url : undefined,
        label: edit.label ? edit.label : undefined,
    };
}

const isNew = computed(() => !props.dataSource);
const isValid = computed(() => isNew.value ? true : !!toInit(innerValue.value));

async function save() {
    fetching.value = true;

    await (isNew.value ? createNew() : updateOld());

    fetching.value = false;
}

async function createNew() {
    const init = toInit(innerValue.value);
    if (!init)
        return;

    const result = await API.dataSources.createDataSource({}, init);
    if (result.status)
        emit('save', result.data);
}

async function updateOld() {
    if (!props.dataSource)
        return;

    const result = await API.dataSources.updateDataSource({ id: props.dataSource.id }, toUpdate(innerValue.value));
    if (result.status)
        emit('save', result.data);
}

function cancel() {
    emit('cancel');
}

async function deleteMethod() {
    if (!props.dataSource)
        return;

    fetching.value = true;
    const result = await API.dataSources.deleteDataSource({ id: props.dataSource.id });
    if (result.status)
        emit('delete');

    fetching.value = false;
}
</script>

<template>
    <div class="editor">
        <h2>{{ isNew ? 'Add' : 'Edit' }} data source</h2>
        <table>
            <tr>
                <td class="label">
                    Type:
                </td>
                <td class="value">
                    <select
                        v-model="innerValue.type"
                        :disabled="!isNew"
                    >
                        <option
                            v-for="availableType in DATA_SOURCE_TYPES"
                            :key="availableType.type"
                            :value="availableType.type"
                        >
                            {{ availableType.label }}
                        </option>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="label">
                    Label:
                </td>
                <td class="value">
                    <input v-model="innerValue.label" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Url:
                </td>
                <td class="value">
                    <input v-model="innerValue.url" />
                </td>
            </tr>
        </table>
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
