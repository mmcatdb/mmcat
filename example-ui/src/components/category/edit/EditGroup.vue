<script lang="ts">
import type { Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';

export default defineComponent({
    components: {

    },
    props: {
        nodes: {
            type: Object as () => Node[],
            required: true
        }
    },
    emits: [ 'save', 'cancel', 'update' ],
    data() {
        return {
        };
    },
    methods: {
        cancel() {
            this.$emit('cancel');
        },
    }
});
</script>

<template>
    <div>
        <h2>Edit Object Group</h2>
        <table>
            <tr>
                <td class="label">
                    Objects:
                </td>
                <td class="value" />
            </tr>
            <tr
                v-for="node in nodes"
                :key="node.schemaObject.id"
            >
                <td class="label">
                    -
                </td>
                <td class="value">
                    {{ node.schemaObject.label }}
                </td>
            </tr>
        </table>
        <div class="button-row">
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>
