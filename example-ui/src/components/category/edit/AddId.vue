<script lang="ts">
import type { Graph, Node } from '@/types/categoryGraph';
import { SchemaIdFactory } from '@/types/identifiers';
import { defineComponent } from 'vue';
import AddSimpleId from './AddSimpleId.vue';
import AddComplexId from './AddComplexId.vue';

/*
 * When the id is simple (it has exactly one signature) the corresponding morphism must have cardinality 1:1.
 * Wheren the id is complex, all its morphisms have to have cardinality n:1 (because otherwise they would be simple identifiers so the complex one wouldn't be needed).
 * The last option is a simple empty identifier.
 */

enum State {
    SelectType,
    Simple,
    Complex
}

export default defineComponent({
    components: {
        AddSimpleId,
        AddComplexId
    },
    props: {
        graph: {
            type: Object as () => Graph,
            required: true
        },
        node: {
            type: Object as () => Node,
            required: true
        }
    },
    emits: [ 'save', 'cancel' ],
    data() {
        return {
            state: State.SelectType,
            State
        };
    },
    methods: {
        save() {
            this.$emit('save');
        },
        cancel() {
            this.$emit('cancel');
        },
        selectSimple() {
            this.state = State.Simple;
        },
        selectComplex() {
            this.state = State.Complex;
        },
        selectEmpty() {
            this.node.addSchemaId(SchemaIdFactory.createEmpty());

            this.$emit('save');
        }
    }
});
</script>

<template>
    <template v-if="state === State.SelectType">
        <h2>Add Id</h2>
        <div class="button-row">
            <button @click="selectSimple">
                Simple
            </button>
            <button @click="selectComplex">
                Complex
            </button>
            <button @click="selectEmpty">
                Empty
            </button>
            <button @click="cancel">
                Cancel
            </button>
        </div>
    </template>
    <template v-else-if="state === State.Simple">
        <AddSimpleId
            :graph="graph"
            :node="node"
            @save="save"
            @cancel="cancel"
        />
    </template>
    <template v-else-if="state === State.Complex">
        <AddComplexId
            :graph="graph"
            :node="node"
            @save="save"
            @cancel="cancel"
        />
    </template>
</template>

<style scoped>

</style>
