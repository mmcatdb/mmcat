<script setup lang="ts">
import { type Database, DB_TYPES, Type, copyDatabaseUpdate, getNewDatabaseUpdate, createInitFromUpdate, type DatabaseUpdate } from '@/types/database';
import API from '@/utils/api';
import { computed, ref } from 'vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

type DatabaseEditorProps = {
    database?: Database;
};

const props = defineProps<DatabaseEditorProps>();

const emit = defineEmits([ 'save', 'cancel', 'delete' ]);

const fetching = ref(false);
const innerValue = ref<DatabaseUpdate>(props.database ? copyDatabaseUpdate(props.database) : getNewDatabaseUpdate());

const isNew = computed(() => !props.database);
const isValid = computed(() => isNew.value ? true : !!createInitFromUpdate(innerValue.value));

async function save() {
    fetching.value = true;

    await (isNew.value ? createNew() : updateOld());

    fetching.value = false;
}

async function createNew() {
    const init = createInitFromUpdate(innerValue.value);
    if (!init)
        return;

    const result = await API.databases.createDatabase({}, init);
    if (result.status)
        emit('save', result.data);
}

async function updateOld() {
    if (!props.database)
        return;

    if (innerValue.value.settings.password === '')
        innerValue.value.settings.password = undefined;

    const result = await API.databases.updateDatabase({ id: props.database.id }, innerValue.value);
    if (result.status)
        emit('save', result.data);
}

function cancel() {
    emit('cancel');
}

async function deleteMethod() {
    if (!props.database)
        return;

    fetching.value = true;
    const result = await API.databases.deleteDatabase({ id: props.database.id });
    if (result.status)
        emit('delete');

    fetching.value = false;
}
</script>

<template>
    <div class="editor">
        <h2>{{ isNew ? 'Add' : 'Edit' }} database</h2>
        <ValueContainer>
            <ValueRow label="Type:">
                <select
                    v-model="innerValue.type"
                    :disabled="!isNew"
                >
                    <option
                        v-for="availableType in DB_TYPES"
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
            <ValueRow label="Host:">
                <input v-model="innerValue.settings.host" />
            </ValueRow>
            <ValueRow label="Port:">
                <input
                    v-model="innerValue.settings.port"
                    type="number"
                />
            </ValueRow>
            <ValueRow label="Database:">
                <input v-model="innerValue.settings.database" />
            </ValueRow>
            <ValueRow
                :class="{ hidden: innerValue.type !== Type.mongodb }"
                label="Authentication Database:"
            >
                <input v-model="innerValue.settings.authenticationDatabase" />
            </ValueRow>
            <ValueRow label="Username:">
                <input v-model="innerValue.settings.username" />
            </ValueRow>
            <ValueRow label="Password:">
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
