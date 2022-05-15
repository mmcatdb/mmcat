<script lang="ts">
import type { Graph, Node } from '@/types/categoryGraph';
import { Key } from '@/types/identifiers';
import { defineComponent } from 'vue';

export default defineComponent({
    components: {

    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        node: {
            type: Object as () => Node,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            label: this.node.label,
            key: this.node.schemaObject.key
        };
    },
    methods: {
        save() {
            // TODO


            //const object = this.graph.schemaCategory.createObject(this.label, this.key, []);
            //this.graph.createNode(object);



            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        confirm() {
            this.save();
        },
        deleteNewNode() {

            // TODO

            this.$emit('save');
        }
    }
});
</script>

<template>
    <div>
        <h2>Edit Schema Object</h2>
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
                    {{ key.value }}
                </td>
            </tr>
            <tr>
                <td class="label">
                    Id:
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                :disabled="!label"
                @click="confirm"
            >
                Confirm
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
            <button
                v-if="node.schemaObject.isNew"
                @click="deleteNewNode"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

