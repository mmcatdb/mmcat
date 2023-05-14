<script setup lang="ts">
import { onUnmounted, ref, watch } from 'vue';
import { SelectionType, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality } from '@/types/schema';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { ObjectIds, Type } from '@/types/identifiers';
import NodeInput from '@/components/input/NodeInput.vue';
import { useEvocat } from '@/utils/injects';

const { evocat, graph } = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const mapLabel = ref('');
const keyLabel = ref('');
const nodes = ref<(Node | undefined)[]>([]);
const temporayEdge = ref<TemporaryEdge | null>(null);

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
    evocat.compositeOperation('addMap', () => {
        const [ node1, node2 ] = nodes.value;
        if (!node1 || !node2)
            return;

        const keyObject = evocat.addObject({
            label: keyLabel.value,
            ids: ObjectIds.createNonSignatures(Type.Value),
        });

        const mapObject = evocat.addObject({
            label: mapLabel.value,
        });

        const mapToKey = evocat.addMorphism({
            dom: mapObject,
            cod: keyObject,
            min: Cardinality.One,
            label: '#key',
        });

        const mapToNode1 = evocat.addMorphism({
            dom: mapObject,
            cod: node1.schemaObject,
            min: Cardinality.One,
        });

        evocat.addMorphism({
            dom: mapObject,
            cod: node2.schemaObject,
            min: Cardinality.One,
        });

        evocat.addId(mapObject, {
            signatures: [ mapToKey.signature, mapToNode1.signature ],
        });
    });

    graph.layout();
    emit('save');
}

function cancel() {
    emit('cancel');
}
</script>

<template>
    <div>
        <h2>Add Map</h2>
        <ValueContainer>
            <ValueRow label="Label:">
                <input v-model="mapLabel" />
            </ValueRow>
            <ValueRow label="Key label:">
                <input v-model="keyLabel" />
            </ValueRow>
            <ValueRow label="Domain object:">
                {{ nodes[0]?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Codomain object:">
                {{ nodes[1]?.schemaObject.label }}
            </ValueRow>
        </ValueContainer>
        <NodeInput
            v-model="nodes"
            :count="2"
            :type="SelectionType.Selected"
        />
        <div class="button-row">
            <button
                :disabled="!mapLabel || !keyLabel || !nodes[0] || !nodes[1]"
                @click="save"
            >
                Confirm
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>
