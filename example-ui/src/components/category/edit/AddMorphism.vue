<script lang="ts">
import type { Graph, Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';

enum State {
    SelectNode1,
    SelectNode2
}

export default defineComponent({
    components: {

    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            node1: null as Node | null,
            node2: null as Node | null,
            state: State.SelectNode1,
            State
        };
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
    },
    unmounted() {
        this.graph.removeListener('tap', this.onNodeTapHandler);
    },
    methods: {
        save() {
            // TODO
            if (!this.node1 || !this.node2)
                return;

            const { morphism, dualMorphism } = this.graph.schemaCategory.createMorphism(this.node1.schemaObject, this.node2.schemaObject);
            this.graph.createEdge(morphism, dualMorphism);

            this.unselectAll();
            this.$emit('save');
        },
        cancel() {
            this.unselectAll();
            this.$emit('cancel');
        },
        confirmNode1() {
            this.state = State.SelectNode2;
        },
        confirmNode2() {
            this.save();
        },
        unselectAll() {
            this.node1?.unselect();
            this.node2?.unselect();
        },
        onNodeTapHandler(node: Node): void {
            if (this.state === State.SelectNode1) {
                if (!node.equals(this.node1)) {
                    this.node1?.unselect();
                    node.select();
                    this.node1 = node;
                }
            }
            else {
                if (!node.equals(this.node2)) {
                    this.node2?.unselect();
                    node.select();
                    this.node2 = node;
                }
            }
        }
    }
});
</script>

<template>
    <div class="outer">
        <h2>Add Schema Morphism</h2>
        <template v-if="node1">
            First node: {{ node1.schemaObject.label }}
        </template>
        <template v-else>
            Select first node.
        </template>
        <br />
        <template v-if="state === State.SelectNode2">
            <template v-if="node2">
                Second node: {{ node2.schemaObject.label }}
            </template>
            <template v-else>
                Select second node.
            </template>
            <br />
        </template>
        <template v-if="state === State.SelectNode1">
            <button
                :disabled="!node1"
                @click="confirmNode1"
            >
                Confirm
            </button>
        </template>
        <template v-else-if="state === State.SelectNode2">
            <button
                :disabled="!node2"
                @click="confirmNode2"
            >
                Confirm
            </button>
        </template>
        <button @click="cancel">
            Cancel
        </button>
    </div>
</template>

<style scoped>
.value {
    font-weight: bold;
}
</style>

