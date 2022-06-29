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
        allowNull: {
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
                    this.setSignature(newValue, false);
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
            if (this.innerValue.isNull)
                this.innerValue = this.innerValue.copyNotNull();

            this.graph.resetAvailabilityStatus();
            this.innerValue.markAvailablePaths(this.filter.function);
            this.sendUpdate();
        },
        setSignature(signature: SequenceSignature, sendUpdate = true) {
            this.innerValue.sequence.unselectAll();
            this.graph.resetAvailabilityStatus();
            this.innerValue = signature;
            this.innerValue.sequence.selectAll();
            this.innerValue.markAvailablePaths(this.filter.function);

            if (sendUpdate)
                this.sendUpdate();
        },
        sendUpdate() {
            this.$emit('update:modelValue', this.innerValue);
            this.$emit('input');
        },
        setSignatureNull() {
            this.setSignature(SequenceSignature.null(this.innerValue.sequence.rootNode));
        },
        setSignatureEmpty() {
            this.setSignature(SequenceSignature.empty(this.innerValue.sequence.rootNode));
        }
    }
});
</script>

<template>
    <button
        v-if="allowNull"
        :disabled="disabled"
        @click="setSignatureNull"
    >
        <slot name="nullButton" />
    </button>
</template>

<style scoped>

</style>

