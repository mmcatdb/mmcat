<script lang="ts">
import { NodeSchemaData, NodeSequence, resetAvailabilityStatus } from '@/types/categoryGraph';
import { TEST_CONFIGURATION } from '@/types/database/Configuration';
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

        // TODO
        this.sequence.lastNode.markAvailablePaths(TEST_CONFIGURATION, this.sequence.lastNode !== this.rootNode);

        //this.sequence.allNodes.forEach(node => node.select());
    },
    unmounted() {
        this.cytoscape.removeListener('tap', this.onNodeTapHandler);
        this.sequence.unselectAll();
        resetAvailabilityStatus(this.cytoscape);
    },
    methods: {
        onNodeTapHandler(event: EventObject): void {
            if (this.disabled)
                return;

            const node = (event.target as NodeSingular).data('schemaData') as NodeSchemaData;
            if (this.sequence.tryRemoveNode(node)) {
                //node.unselect();
                resetAvailabilityStatus(this.cytoscape);
                this.sequence.lastNode.markAvailablePaths(TEST_CONFIGURATION, this.sequence.lastNode !== this.rootNode);
                this.updateInnerValueFromSequenceChange();
            }
            else if (this.sequence.tryAddNode(node)) {
                //node.select();
                resetAvailabilityStatus(this.cytoscape);
                this.sequence.lastNode.markAvailablePaths(TEST_CONFIGURATION, this.sequence.lastNode !== this.rootNode);
                this.updateInnerValueFromSequenceChange();
            }
        },
        sequenceFromSignature(signature: Signature): NodeSequence {
            const sequence = NodeSequence.withRootNode(this.rootNode);
            sequence.addSignature(signature);
            return sequence;
        },
        setSignature(signature: Signature, sendUpdate = true) {
            //this.sequence.allNodes.forEach(node => node.unselect());
            this.sequence.unselectAll();
            resetAvailabilityStatus(this.cytoscape);
            this.sequence = this.sequenceFromSignature(signature);
            this.sequence.lastNode.markAvailablePaths(TEST_CONFIGURATION, this.sequence.lastNode !== this.rootNode);
            this.innerValue = signature;
            //this.sequence.allNodes.forEach(node => node.select());

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

