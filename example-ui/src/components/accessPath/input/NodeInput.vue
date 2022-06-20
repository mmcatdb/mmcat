<script lang="ts">
import { SelectionType, Graph, Node } from '@/types/categoryGraph';
import { defineComponent } from 'vue';

export default defineComponent({
    props: {
        graph: {
            type: Graph,
            required: true
        },
        modelValue: {
            type: Object as () => Node | null,
            default: null,
            required: false
        }
    },
    emits: [ 'update:modelValue' ],
    data() {
        return {
            innerValue: null as Node | null,
        };
    },
    watch: {
        modelValue: {
            handler(newValue: Node | null): void {
                if (!newValue) {
                    this.innerValue?.unselect();
                    this.innerValue = null;
                    return;
                }

                if (newValue.equals(this.innerValue))
                    return;

                this.innerValue?.unselect();
                this.innerValue = newValue;
                this.innerValue.select({ type: SelectionType.Root, level: 0 });
            }
        }
    },
    mounted() {
        this.graph.addNodeListener('tap', this.onNodeTapHandler);
    },
    unmounted() {
        this.graph.removeNodeListener('tap', this.onNodeTapHandler);
    },
    methods: {
        onNodeTapHandler(node: Node) {
            this.innerValue?.unselect();

            if (node.equals(this.innerValue)) {
                // If we double tap current node, it become unselected.
                this.innerValue = null;
            }
            else {
                node.select({ type: SelectionType.Root, level: 0 });
                this.innerValue = node;
            }

            this.$emit('update:modelValue', this.innerValue);
        }
    }
});
</script>

<template>
    <span>
        {{ innerValue?.schemaObject.label }}
    </span>
</template>
