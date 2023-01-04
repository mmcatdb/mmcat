<script lang="ts">
import type { Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';

export default defineComponent({
    components: {
        ValueContainer,
        ValueRow
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
        <ValueContainer>
            <ValueRow label="Objects:" />
            <ValueRow
                v-for="node in nodes"
                :key="node.schemaObject.id"
                label="-"
            >
                {{ node.schemaObject.label }}
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>
