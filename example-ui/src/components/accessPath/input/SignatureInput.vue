<script lang="ts">
import { Graph, Node, NodeSequence } from '@/types/categoryGraph';
import type { Database } from '@/types/database';
import { Signature } from '@/types/identifiers';
import { defineComponent } from 'vue';

type OutputType = { signature: Signature, node?: Node };

export default defineComponent({
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        database: {
            type: Object as () => Database,
            required: true
        },
        rootNode: {
            type: Object as () => Node,
            required: true
        },
        modelValue: {
            type: Object as () => OutputType,
            default: () => ({ signature: Signature.empty })
        },
        disabled: {
            type: Boolean,
            default: false,
            required: false
        }
    },
    emits: [ 'update:modelValue', 'input' ],
    data() {
        const sequence = this.sequenceFromSignature(this.modelValue.signature);

        return {
            sequence: sequence as NodeSequence,
            innerValue: { signature: sequence.toSignature(), node: sequence.lastNode },
            Signature: Signature
        };
    },
    watch: {
        modelValue: {
            handler(newValue: OutputType): void {
                if (!newValue.signature.equals(this.innerValue.signature))
                    this.setSignature(newValue.signature, false);
            }
        }
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);

        // TODO
        this.sequence.lastNode.markAvailablePaths(this.database.configuration, this.sequence.lastNode !== this.rootNode);

        //this.sequence.allNodes.forEach(node => node.select());
    },
    unmounted() {
        this.graph.removeListener('tap', this.onNodeTapHandler);
        this.sequence.unselectAll();
        this.graph.resetAvailabilityStatus();
    },
    methods: {
        onNodeTapHandler(node: Node): void {
            if (this.disabled)
                return;

            if (this.sequence.tryRemoveNode(node)) {
                //node.unselect();
                this.graph.resetAvailabilityStatus();
                this.sequence.lastNode.markAvailablePaths(this.database.configuration, this.sequence.lastNode !== this.rootNode);
                this.updateInnerValueFromSequenceChange();
            }
            else if (this.sequence.tryAddNode(node)) {
                //node.select();
                this.graph.resetAvailabilityStatus();
                this.sequence.lastNode.markAvailablePaths(this.database.configuration, this.sequence.lastNode !== this.rootNode);
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
            this.graph.resetAvailabilityStatus();
            this.sequence = this.sequenceFromSignature(signature);
            this.sequence.lastNode.markAvailablePaths(this.database.configuration, this.sequence.lastNode !== this.rootNode);
            this.innerValue = { signature, node: this.sequence.lastNode };
            //this.sequence.allNodes.forEach(node => node.select());

            if (sendUpdate) {
                this.$emit('update:modelValue', this.innerValue);
                this.$emit('input');
            }
        },
        updateInnerValueFromSequenceChange() {
            this.innerValue = { signature: this.sequence.toSignature(), node: this.sequence.lastNode };
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

