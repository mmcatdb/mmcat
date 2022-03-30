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
        modelValue: {
            type: Object as () => NodeSequence,
            required: true
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            innerValue: NodeSequence.copy(this.modelValue),
        };
    },
    watch: {
        modelValue: {
            handler(newValue: NodeSequence, oldValue: NodeSequence): void {
                oldValue.allNodes.forEach(node => node.select());
                this.innerValue = NodeSequence.copy(newValue);
                this.innerValue.allNodes.forEach(node => node.select());
            }
        }
    },
    mounted() {
        this.cytoscape.addListener('tap', 'node', this.onNodeTapHandler);
        this.innerValue.allNodes.forEach(node => node.select());
    },
    unmounted() {
        this.cytoscape.removeListener('tap', this.onNodeTapHandler);
        this.innerValue.allNodes.forEach(node => node.unselect());
    },
    methods: {
        onNodeTapHandler(event: EventObject): void {
            const node = (event.target as NodeSingular).data('schemaData') as NodeSchemaData;
            if (this.innerValue.tryRemoveNode(node)) {
                node.unselect();
                this.$emit('update:modelValue', NodeSequence.copy(this.innerValue as NodeSequence));
            }
            else if (this.innerValue.tryAddNode(node)) {
                node.select();
                this.$emit('update:modelValue', NodeSequence.copy(this.innerValue as NodeSequence));
            }
        }
    }
});
</script>

<template>
    <div class="outer" />
</template>

<style scoped>
.outer {
    display: none;
}
</style>

