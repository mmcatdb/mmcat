<script setup lang="ts">
import { SelectionType, type Graph, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality, type Min } from '@/types/schema';
import { onUnmounted, ref, watch } from 'vue';
import MinimumInput from './MinimumInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { computed } from '@vue/reactivity';
import NodeInput from '@/components/input/NodeInput.vue';

type AddMorphismProps = {
    graph: Graph;
};

const props = defineProps<AddMorphismProps>();

const emit = defineEmits([ 'save', 'cancel' ]);


const label = ref('');
const iri = ref('');
const pimIri = ref('');
const nodes = ref<(Node | undefined)[]>([]);
const temporayEdge = ref<TemporaryEdge | null>(null);
const min = ref<Min>(Cardinality.One);

const nodesSelected = computed(() => !!nodes.value[0] && !!nodes.value[1]);
const iriIsAvailable = computed(() => props.graph.schemaCategory.iriIsAvailable(iri.value));

watch(nodes, (newValue, oldValue) => {
    if (newValue[0] === oldValue[0] && newValue[1] === oldValue[1])
        return;

    temporayEdge.value?.delete();

    if (!newValue[0] || !newValue[1])
        return;

    temporayEdge.value = props.graph.createTemporaryEdge(newValue[0], newValue[1]);
});

onUnmounted(() => {
    temporayEdge.value?.delete();
});

function save() {
    const [ node1, node2 ] = nodes.value;
    if (!node1 || !node2)
        return;

    if (iri.value) {
        const morphism = props.graph.schemaCategory.createMorphismWithIri(node1.schemaObject, node2.schemaObject, min.value, iri.value, pimIri.value, label.value);
        if (!morphism)
            return;

        temporayEdge.value?.delete();
        props.graph.createEdge(morphism, 'new');
    }
    else {
        const morphism = props.graph.schemaCategory.createMorphism(node1.schemaObject, node2.schemaObject, min.value, label.value);
        temporayEdge.value?.delete();
        props.graph.createEdge(morphism, 'new');
    }

    emit('save');
}

function cancel() {
    emit('cancel');
}

function switchNodes() {
    nodes.value = [ nodes.value[1], nodes.value[0] ];
}
</script>

<template>
    <div class="add-morphism">
        <h2>Add Schema Morphism</h2>
        <ValueContainer>
            <ValueRow label="Domain object:">
                {{ nodes[0]?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Codomain object:">
                {{ nodes[1]?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Label?:">
                <input v-model="label" />
            </ValueRow>
            <ValueRow label="Iri?:">
                <input v-model="iri" />
            </ValueRow>
            <ValueRow label="Pim Iri?:">
                <input v-model="pimIri" />
            </ValueRow>
            <MinimumInput
                v-model="min"
            />
        </ValueContainer>
        <NodeInput
            v-model="nodes"
            :graph="graph"
            :count="2"
            :type="SelectionType.Selected"
        />
        <div class="button-row">
            <button
                :disabled="!nodesSelected || !iriIsAvailable"
                @click="save"
            >
                Confirm
            </button>
            <button
                :disabled="!nodesSelected"
                @click="switchNodes"
            >
                Switch
            </button>
            <button @click="cancel">
                Cancel
            </button>
        </div>
    </div>
</template>

<style>
.number-input {
    max-width: 80px;
}
</style>

