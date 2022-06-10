<script lang="ts">
import { defineComponent } from 'vue';
import { type Database, DB_TYPES, Type, copyDatabaseUpdate, getNewDatabaseUpdate } from '@/types/database';
import { DELETE, POST, PUT } from '@/utils/backendAPI';


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
            return !this.isNew || (
                this.innerValue.type != null &&
                !!this.innerValue.label &&
                !!this.innerValue.settings.host &&
                !!this.innerValue.settings.port &&
                !!this.innerValue.settings.database &&
                !!this.innerValue.settings.username &&
                !!this.innerValue.settings.password && (
                    !!this.innerValue.settings.authenticationDatabase ||
                    this.innerValue.type !== Type.mongodb
                )
            );
        }
    },
    watch: {
        /*
        portString: {
            handler(newValue: string, oldValue: string): void {
                console.log({ newValue, oldValue });
                if (!newValue.match(/^[0-9]*$/)) {
                    console.log("nope");
                    this.portString = oldValue;
                    return;
                }

                this.innerValue.settings.port = parseInt(newValue);
                console.log("A");
            }
        }
        */
    },
    methods: {
        async save() {
            this.fetching = true;
            const result = await (this.isNew ? this.createNew() : this.updateOld());
            if (result.status)
                this.$emit('save', result.data);

            this.fetching = false;
        },
        async createNew() {
            return await POST<Database>('/databases', this.innerValue);
        },
        async updateOld() {
            return await PUT<Database>(`/databases/${this.database?.id}`, this.innerValue);
        },
        cancel() {
            this.$emit('cancel');
        },
        async deleteMethod() {
            this.fetching = true;
            const result = await DELETE<void>(`/databases/${this.database?.id}`);
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
