<script lang="ts">
import type { NodeSchemaData } from '@/types/categoryGraph';
import type { Core, EventObject, NodeSingular } from 'cytoscape';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        cytoscape: {
            type: Object as () => Core,
            required: true
        }
    },
    emits: [ 'rootNode:confirm' ],
    data() {
        return {
            lastClickedNode: null as NodeSchemaData | null,
        };
    },
    mounted() {
        this.cytoscape.addListener('tap', 'node', this.onNodeTapHandler);
    },
    unmounted() {
        this.cytoscape.removeListener('tap', this.onNodeTapHandler);
    },
    methods: {
        onNodeTapHandler(event: EventObject): void {
            if (this.lastClickedNode)
                this.lastClickedNode.unselect();

            const node = (event.target as NodeSingular).data('schemaData') as NodeSchemaData;
            if (node.equals(this.lastClickedNode)) {
                // If we double tap current node, it become unselected.
                this.lastClickedNode = null;
            }
            else {
                node.select();
                this.lastClickedNode = node;
            }
        },
        confirm() {
            this.lastClickedNode?.unselect();
            this.lastClickedNode?.becomeRoot();
            this.$emit('rootNode:confirm', this.lastClickedNode);
        }
    }
});
</script>

<template>
    <div class="outer">
        Choose root object: {{ lastClickedNode?.schemaObject.label }}<br>
        <button
            :disabled="!lastClickedNode"
            @click="confirm"
        >
            Confirm
        </button>
    </div>
</template>

<style scoped>
.outer {

}
</style>

