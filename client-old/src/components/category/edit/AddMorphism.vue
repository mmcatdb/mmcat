<script setup lang="ts">
import { SelectionType, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality, type Min } from '@/types/schema';
import { onUnmounted, ref, shallowRef, watch } from 'vue';
import MinimumInput from './MinimumInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { computed } from '@vue/reactivity';
import NodeInput from '@/components/input/NodeInput.vue';
import { useEvocat } from '@/utils/injects';

const { evocat, graph } = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const label = ref('');
const nodes = shallowRef<(Node | undefined)[]>([]);
const temporayEdge = ref<TemporaryEdge | null>(null);
const min = ref<Min>(Cardinality.One);

const nodesSelected = computed(() => !!nodes.value[0] && !!nodes.value[1]);

watch(nodes, (newValue, oldValue) => {
    if (newValue[0] === oldValue[0] && newValue[1] === oldValue[1])
        return;

    temporayEdge.value?.delete();

    if (!newValue[0] || !newValue[1])
        return;

    temporayEdge.value = graph.createTemporaryEdge(newValue[0], newValue[1]);
});

onUnmounted(() => {
    temporayEdge.value?.delete();
});

function save() {
    const [ node1, node2 ] = nodes.value;
    if (!node1 || !node2)
        return;

    evocat.createMorphism({
        domKey: node1.schemaObjex.key,
        codKey: node2.schemaObjex.key,
        min: min.value,
        label: label.value,
    });

    temporayEdge.value?.delete();
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
                {{ nodes[0]?.metadata.label }}
            </ValueRow>
            <ValueRow label="Codomain object:">
                {{ nodes[1]?.metadata.label }}
            </ValueRow>
            <ValueRow label="Label?:">
                <input v-model="label" />
            </ValueRow>
            <MinimumInput
                v-model="min"
            />
        </ValueContainer>
        <NodeInput
            v-model="nodes"
            :count="2"
            :type="SelectionType.Selected"
        />
        <div class="button-row">
            <button
                :disabled="!nodesSelected"
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

