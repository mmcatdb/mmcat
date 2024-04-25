<script setup lang="ts">
import { type Datasource, DATASOURCE_TYPES, DatasourceType, copyDatasourceUpdate, getNewDatasourceUpdate, createInitFromUpdate, type DatasourceUpdate, isDatabase, isFile } from '@/types/datasource';
import API from '@/utils/api';
import { computed, ref } from 'vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

type DatasourceEditorProps = {
    datasource?: Datasource;
};

const props = defineProps<DatasourceEditorProps>();

const emit = defineEmits([ 'save', 'cancel', 'delete' ]);

const fetching = ref(false);
const innerValue = ref<DatasourceUpdate>(props.datasource ? copyDatasourceUpdate(props.datasource) : getNewDatasourceUpdate());

const isNew = computed(() => !props.datasource);
const isValid = computed(() => isNew.value ? true : !!createInitFromUpdate(innerValue.value));

const showDatabaseOptions = computed(() => innerValue.value.type && isDatabase(innerValue.value.type));
const showURLOptions = computed(() => innerValue.value.type && isFile(innerValue.value.type));

async function save() {
    fetching.value = true;

    await (isNew.value ? createNew() : updateOld());

    fetching.value = false;
}

async function createNew() {
    const init = createInitFromUpdate(innerValue.value);
    if (!init)
        return;

    const result = await API.datasources.createDatasource({}, init);
    if (result.status)
        emit('save', result.data);
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
        <h2>{{ isNew ? 'Add' : 'Edit' }} Data Source</h2>
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
                v-if="showURLOptions"
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
                label="Authentication Database:"
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
