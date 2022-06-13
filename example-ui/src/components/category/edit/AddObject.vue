<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
import { Key } from '@/types/identifiers';
import { defineComponent } from 'vue';

export default defineComponent({
    components: {

    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        const key = this.graph.schemaCategory.suggestKey();
        return {
            label: '',
            key,
            keyValue: key.value,
            keyIsValid: true
        };
    },
    methods: {
        save() {
            const object = this.graph.schemaCategory.createObject(this.label, this.key, []);
            this.graph.createNode(object, 'new');

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        keyValueChanged() {
            this.key = Key.createNew(this.keyValue);
            this.keyIsValid = this.graph.schemaCategory.isKeyAvailable(this.key);
        }
    }
});
</script>

<template>
    <div>
        <h2>Add Schema Object</h2>
        <table>
            <tr>
                <td class="label">
                    Label:
                </td>
                <td class="value">
                    <input
                        v-model="label"
                    />
                </td>
            </tr>
            <tr>
                <td class="label">
                    Key:
                </td>
                <td class="value">
                    <input
                        v-model="keyValue"
                        class="number-input"
                        type="number"
                        min="0"
                        step="1"
                        @input="keyValueChanged"
                    />
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                :disabled="!keyIsValid || !label"
                @click="save"
            >
                Confirm
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

