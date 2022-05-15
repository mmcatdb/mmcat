<script lang="ts">
import type { RootProperty } from '@/types/accessPath/graph';
import type { Graph } from '@/types/categoryGraph';
import type { StaticName } from '@/types/identifiers';
import { defineComponent } from 'vue';
import StaticNameInput from '../input/StaticNameInput.vue';
import type { Database } from '@/types/database';

export default defineComponent({
    components: {
        StaticNameInput
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        database: {
            type: Object as () => Database,
            required: true
        },
        property: {
            type: Object as () => RootProperty,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            name: this.property.name.copy() as StaticName
        };
    },
    computed: {
        nameChanged(): boolean {
            return !this.property.name.equals(this.name);
        }
    },
    methods: {
        save() {
            if (this.nameChanged)
                this.property.update(this.name);

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        confirmName() {
            this.save();
        },
        resetName() {
            this.name = this.property.name.copy();
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Edit root property</h2>
        <table>
            <tr>
                <td class="label">
                    Name:
                </td>
                <td class="value">
                    <StaticNameInput
                        v-model="name"
                    />
                </td>
            </tr>
        </table>
        <br />
        <button
            @click="confirmName"
        >
            {{ nameChanged ? 'Confirm change' : 'Keep current' }}
        </button>
        <button
            v-if="nameChanged"
            @click="resetName"
        >
            Reset
        </button>
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>

<style scoped>
.selected {
    font-weight: bold;
}
</style>

