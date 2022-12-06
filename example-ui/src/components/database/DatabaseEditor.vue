<script lang="ts">
import { defineComponent } from 'vue';
import { type Database, DB_TYPES, Type, copyDatabaseUpdate, getNewDatabaseUpdate, createInitFromUpdate } from '@/types/database';
import API from '@/utils/api';

export default defineComponent({
    props: {
        database: {
            type: Object as () => Database | null,
            default: null,
            required: false
        }
    },
    emits: [ 'save', 'cancel', 'delete' ],
    data() {
        return {
            fetching: false,
            innerValue: this.database !== null ? copyDatabaseUpdate(this.database) : getNewDatabaseUpdate(),
            //portString: '',
            DB_TYPES,
            Type
        };
    },
    computed: {
        isNew(): boolean {
            return this.database === null;
        },
        isValid(): boolean {
            return this.isNew ? true : !!createInitFromUpdate(this.innerValue);
        }
    },
    methods: {
        async save() {
            this.fetching = true;

            await (this.isNew ? this.createNew() : this.updateOld());

            this.fetching = false;
        },
        async createNew() {
            const init = createInitFromUpdate(this.innerValue);
            if (!init)
                return;

            const result = await API.databases.createDatabase({}, init);
            if (result.status)
                this.$emit('save', result.data);
        },
        async updateOld() {
            if (!this.database)
                return;

            if (this.innerValue.settings.password === '')
                this.innerValue.settings.password = undefined;

            const result = await API.databases.updateDatabase({ id: this.database.id }, this.innerValue);
            if (result.status)
                this.$emit('save', result.data);
        },
        cancel() {
            this.$emit('cancel');
        },
        async deleteMethod() {
            if (!this.database)
                return;

            this.fetching = true;
            const result = await API.databases.deleteDatabase({ id: this.database.id });
            if (result.status)
                this.$emit('delete');

            this.fetching = false;
        }
    }
});
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

<style scoped>
.accessPathInput {
    color: white;
    background-color: black;
    width: 600px;
    height: 600px;
    font-size: 15px;
}
</style>
