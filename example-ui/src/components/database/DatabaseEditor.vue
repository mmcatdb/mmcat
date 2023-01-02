<script setup lang="ts">
import { type Database, DB_TYPES, Type, copyDatabaseUpdate, getNewDatabaseUpdate, createInitFromUpdate, type DatabaseUpdate } from '@/types/database';
import API from '@/utils/api';
import { computed, ref } from 'vue';

interface DatabaseEditorProps {
    database?: Database;
}

const props = defineProps<DatabaseEditorProps>();

const emit = defineEmits([ 'save', 'cancel', 'delete' ]);

const fetching = ref(false);
const innerValue = ref<DatabaseUpdate>(props.database ? copyDatabaseUpdate(props.database) : getNewDatabaseUpdate());

const isNew = computed(() => props.database === null);
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
                            v-for="availableType in DB_TYPES"
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
                    Host:
                </td>
                <td class="value">
                    <input v-model="innerValue.settings.host" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Port:
                </td>
                <td class="value">
                    <input
                        v-model="innerValue.settings.port"
                        type="number"
                    />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Database:
                </td>
                <td class="value">
                    <input v-model="innerValue.settings.database" />
                </td>
            </tr>
            <tr :class="{ hidden: innerValue.type !== Type.mongodb }">
                <td class="label">
                    Authentication Database:
                </td>
                <td class="value">
                    <input v-model="innerValue.settings.authenticationDatabase" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Username:
                </td>
                <td class="value">
                    <input v-model="innerValue.settings.username" />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Password:
                </td>
                <td class="value">
                    <input v-model="innerValue.settings.password" />
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
