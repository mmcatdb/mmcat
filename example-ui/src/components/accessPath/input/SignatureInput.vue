<script lang="ts">
import { NodeSchemaData, NodeSequence } from '@/types/categoryGraph';
import { Signature } from '@/types/identifiers';
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
        },
        modelValue: {
            type: Object as () => Signature,
            default: Signature.empty
        },
        disabled: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'update:modelValue', 'input' ],
    data() {
        const sequence = this.sequenceFromSignature(this.modelValue);

        return {
            sequence: sequence as NodeSequence,
            innerValue: sequence.toSignature(),
            Signature: Signature
        };
    },
    watch: {
        modelValue: {
            handler(newValue: Signature): void {
                if (!newValue.equals(this.innerValue))
                    this.setSignature(newValue, false);
            }
        }
    },
    mounted() {
        this.cytoscape.addListener('tap', 'node', this.onNodeTapHandler);
        this.sequence.allNodes.forEach(node => node.select());
    },
    unmounted() {
        this.cytoscape.removeListener('tap', this.onNodeTapHandler);
        this.sequence.allNodes.forEach(node => node.unselect());
    },
    methods: {
        onNodeTapHandler(event: EventObject): void {
            if (this.disabled)
                return;

            const node = (event.target as NodeSingular).data('schemaData') as NodeSchemaData;
            if (this.sequence.tryRemoveNode(node)) {
                node.unselect();
                this.updateInnerValueFromSequenceChange();
            }
            else if (this.sequence.tryAddNode(node)) {
                node.select();
                this.updateInnerValueFromSequenceChange();
            }
        },
        sequenceFromSignature(signature: Signature): NodeSequence {
            const sequence = NodeSequence.withRootNode(this.rootNode);
            sequence.addSignature(signature);
            return sequence;
        },
        setSignature(signature: Signature, sendUpdate = true) {
            this.sequence.allNodes.forEach(node => node.unselect());
            this.sequence = this.sequenceFromSignature(signature);
            this.innerValue = signature;
            this.sequence.allNodes.forEach(node => node.select());

            if (sendUpdate) {
                this.$emit('update:modelValue', this.innerValue);
                this.$emit('input');
            }
        },
        updateInnerValueFromSequenceChange() {
            this.innerValue = this.sequence.toSignature();
            this.$emit('update:modelValue', this.innerValue);
            this.$emit('input');
        }
    }
});
</script>

<template>
    <div class="outer">
        <template v-if="!disabled">
            <button
                @click="() => setSignature(Signature.null)"
            >
                Null
            </button>
            <button
                @click="() => setSignature(Signature.empty)"
            >
                Empty
            </button>
        </template>
    </div>
</template>

<style scoped>
.outer {
    background-color: darkblue;
    padding: 12px;
}
</style>

