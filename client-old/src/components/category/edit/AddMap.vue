<script setup lang="ts">
import { onUnmounted, ref, shallowRef, watch } from 'vue';
import { SelectionType, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality } from '@/types/schema';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { ObjexIds, Type } from '@/types/identifiers';
import NodeInput from '@/components/input/NodeInput.vue';
import { useEvocat } from '@/utils/injects';

const { evocat, graph } = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const mapLabel = ref('');
const keyLabel = ref('');
const nodes = shallowRef<(Node | undefined)[]>([]);
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
        const [ sourceNode, valueNode ] = nodes.value;
        if (!sourceNode || !valueNode || !sourceNode.schemaObjex.ids)
            return;

        const keyObjexIds = ObjexIds.createNonSignatures(Type.Value);
        const keyObjex = evocat.createObjex({
            label: keyLabel.value,
            ids: keyObjexIds,
        });

        const mapObjex = evocat.createObjex({
            label: mapLabel.value,
        });

        const mapToKey = evocat.createMorphism({
            domKey: mapObjex.key,
            codKey: keyObjex.key,
            min: Cardinality.One,
            label: '#key',
        });

        const mapToSourceNode = evocat.createMorphism({
            domKey: mapObjex.key,
            codKey: sourceNode.schemaObjex.key,
            min: Cardinality.One,
        });

        evocat.createMorphism({
            domKey: mapObjex.key,
            codKey: valueNode.schemaObjex.key,
            min: Cardinality.One,
        });

        evocat.updateObjex(mapObjex, {
            ids: ObjexIds.createCrossProduct([
                { signature: mapToKey.signature, ids: keyObjexIds },
                { signature: mapToSourceNode.signature, ids: sourceNode.schemaObjex.ids },
            ]),
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
                {{ nodes[0]?.metadata.label }}
            </ValueRow>
            <ValueRow label="Codomain object:">
                {{ nodes[1]?.metadata.label }}
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
