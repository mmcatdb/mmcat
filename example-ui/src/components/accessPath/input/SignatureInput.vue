<script lang="ts">
import type { Graph, Node } from '@/types/categoryGraph';
import { SequenceSignature } from '@/types/accessPath/graph';
import { defineComponent } from 'vue';
import type { Filter } from '@/types/categoryGraph';

export default defineComponent({
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        filters: {
            type: Object as () => Filter | Filter[],
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
        this.innerValue.sequence.selectAll();
        this.innerValue.markAvailablePaths(this.filters);
    },
    unmounted() {
        this.graph.removeNodeListener('tap', this.onNodeTapHandler);
        this.innerValue.sequence.unselectAll();
        this.graph.resetAvailabilityStatus();
    },
    methods: {
        onNodeTapHandler(node: Node): void {
            if (this.disabled)
                return;

            if (this.innerValue.sequence.tryRemoveNode(node)) {
                if (this.innerValue.isNull)
                    this.innerValue = this.innerValue.copyNotNull();

                this.graph.resetAvailabilityStatus();
                this.innerValue.markAvailablePaths(this.filters);
                this.sendUpdate();
            }
            else if (this.innerValue.sequence.tryAddNode(node)) {
                if (this.innerValue.isNull)
                    this.innerValue = this.innerValue.copyNotNull();

                this.graph.resetAvailabilityStatus();
                this.innerValue.markAvailablePaths(this.filters);
                this.sendUpdate();
            }
        },
        setSignature(signature: SequenceSignature, sendUpdate = true) {
            this.innerValue.sequence.unselectAll();
            this.graph.resetAvailabilityStatus();
            this.innerValue = signature;
            this.innerValue.sequence.selectAll();
            this.innerValue.markAvailablePaths(this.filters);

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

