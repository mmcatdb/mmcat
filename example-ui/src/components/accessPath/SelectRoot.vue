<script lang="ts">
import { SelectionType, type Graph, type Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    emits: [ 'rootNode:confirm' ],
    data() {
        return {
            lastClickedNode: null as Node | null,
        };
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
    },
    unmounted() {
        this.graph.removeListener('tap', this.onNodeTapHandler);
    },
    methods: {
        onNodeTapHandler(node: Node): void {
            if (this.lastClickedNode)
                this.lastClickedNode.unselect();

            if (node.equals(this.lastClickedNode)) {
                // If we double tap current node, it become unselected.
                this.lastClickedNode = null;
            }
            else {
                node.select({ type: SelectionType.Root, level: 0 });
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
        Choose root object: {{ lastClickedNode?.schemaObject.label }}
        <br />
        <button
            :disabled="!lastClickedNode"
            @click="confirm"
        >
            Confirm
        </button>
    </div>
</template>

<style scoped>

</style>

