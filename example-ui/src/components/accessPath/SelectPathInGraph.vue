<script lang="ts">
import { NodeSequence, type NodeSchemaData } from '@/types/categoryGraph';
import type { Core, EventObject, NodeSingular } from 'cytoscape';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        cytoscape: {
            type: Object as () => Core,
            required: true
        },
        rootNode: {
            type: Object as () => NodeSchemaData,
            required: true
        }
    },
    emits: [ 'pathInGraph:update', 'pathInGraph:confirm', 'pathInGraph:cancel' ],
    data() {
        return {
            sequence: NodeSequence.withRootNode(this.rootNode),
        };
    },
    mounted() {
        this.cytoscape.addListener('tap', 'node', this.onNodeTapHandler);
        this.rootNode.select();
    },
    unmounted() {
        this.cytoscape.removeListener('tap', this.onNodeTapHandler);
    },
    methods: {
        onNodeTapHandler(event: EventObject): void {
            const node = (event.target as NodeSingular).data('schemaData') as NodeSchemaData;
            if (this.sequence.tryRemoveNode(node)) {
                node.unselect();
                this.$emit('pathInGraph:update', this.sequence);
            }
            else if (this.sequence.tryAddNode(node)) {
                node.select();
                this.$emit('pathInGraph:update', this.sequence);
            }
        },
        confirm() {
            this.sequence.allNodes.forEach(node => node.unselect());
            this.$emit('pathInGraph:confirm', this.sequence.toSignature());
        },
        cancel() {
            this.sequence.allNodes.forEach(node => node.unselect());
            this.$emit('pathInGraph:cancel');
        }
    }
});
</script>

<template>
    <div class="outer">
        <button
            :disabled="sequence.allNodes.length < 2"
            @click="confirm"
        >
            Confirm
        </button>
        <button
            @click="cancel"
        >
            Cancel
        </button>
    </div>
</template>

<style scoped>
.outer {

}
</style>

