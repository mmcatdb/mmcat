<script lang="ts">
import type { Edge, Graph, Node, FilterFunction } from '@/types/categoryGraph';
import { SequenceSignature } from '@/types/accessPath/graph';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        filter: {
            type: Object as () => { function: FilterFunction | FilterFunction[] },
            required: true
        },
        modelValue: {
            type: Object as () => SequenceSignature,
            required: true
        },
        defaultIsNull: {
            type: Boolean,
            default: false,
            required: false
        },
        disabled: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'update:modelValue', 'input' ],
    data() {
        return {
            innerValue: this.modelValue.copy()
        };
    },
    watch: {
        modelValue: {
            handler(newValue: SequenceSignature): void {
                if (!this.innerValue.equals(newValue))
                    this.setSignature(newValue);
            }
        }
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
        this.graph.addEdgeListener('tap', this.onEdgeTapHandler);
        this.innerValue.sequence.selectAll();
        this.innerValue.markAvailablePaths(this.filter.function);
    },
    unmounted() {
        this.graph.removeNodeListener('tap', this.onNodeTapHandler);
        this.graph.removeEdgeListener('tap', this.onEdgeTapHandler);
        this.innerValue.sequence.unselectAll();
        this.graph.resetAvailabilityStatus();
    },
    methods: {
        onNodeTapHandler(node: Node): void {
            if (this.disabled)
                return;

            if (this.innerValue.sequence.tryRemoveNode(node) || this.innerValue.sequence.tryAddNode(node))
                this.updateInnerValue();
        },
        onEdgeTapHandler(edge: Edge): void {
            if (this.disabled)
                return;

            if (this.innerValue.sequence.tryAddEdge(edge) || this.innerValue.sequence.tryAddEdge(edge.dual))
                this.updateInnerValue();
        },
        updateInnerValue() {
            if (!this.innerValue.isEmpty && this.innerValue.isNull)
                this.innerValue = this.innerValue.copyNotNull();

            if (this.innerValue.isEmpty && this.defaultIsNull)
                this.innerValue = SequenceSignature.null(this.innerValue.sequence.rootNode);

            this.graph.resetAvailabilityStatus();
            this.innerValue.markAvailablePaths(this.filter.function);
            this.sendUpdate();
        },
        setSignature(signature: SequenceSignature) {
            this.innerValue.sequence.unselectAll();
            this.graph.resetAvailabilityStatus();
            this.innerValue = signature;
            this.innerValue.sequence.selectAll();
            this.innerValue.markAvailablePaths(this.filter.function);
        },
        sendUpdate() {
            this.$emit('update:modelValue', this.innerValue);
            this.$emit('input');
        }
    }
});
</script>

<template>
    <div v-if="false" />
</template>

