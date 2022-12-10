<script lang="ts">
import type { Graph } from '@/types/categoryGraph';
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
        return {
            label: '',
            keyIsValid: true
        };
    },
    methods: {
        save() {
            const object = this.graph.schemaCategory.createObject(this.label, []);
            this.graph.createNode(object, 'new');
            this.graph.layout();

            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
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

