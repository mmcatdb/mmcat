<script setup lang="ts">
import { onUnmounted, ref, watch } from 'vue';
import { SelectionType, type Graph, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality } from '@/types/schema';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { ObjectIds, SignatureId, Type } from '@/types/identifiers';
import NodeInput from '@/components/input/NodeInput.vue';

type AddMapProps = {
    graph: Graph;
};

const props = defineProps<AddMapProps>();

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

    temporayEdge.value = props.graph.createTemporaryEdge(newValue[0], newValue[1]);
});

onUnmounted(() => {
    temporayEdge.value?.delete();
});

function save() {
    const [ node1, node2 ] = nodes.value;
    if (!node1 || !node2)
        return;

    const keyObject = props.graph.schemaCategory.createObject(keyLabel.value, ObjectIds.createNonSignatures(Type.Value));
    props.graph.createNode(keyObject, 'new');

    const mapObject = props.graph.schemaCategory.createObject(mapLabel.value);
    const mapNode = props.graph.createNode(mapObject, 'new');

    const mapToKey = props.graph.schemaCategory.createMorphism(mapObject, keyObject, Cardinality.One, '#key');
    props.graph.createEdge(mapToKey, 'new');

    const mapToNode1 = props.graph.schemaCategory.createMorphism(mapObject, node1.schemaObject, Cardinality.One, '');
    props.graph.createEdge(mapToNode1, 'new');
    const mapToNode2 = props.graph.schemaCategory.createMorphism(mapObject, node2.schemaObject, Cardinality.One, '#value');
    props.graph.createEdge(mapToNode2, 'new');

    mapNode.addSignatureId(new SignatureId([ mapToKey.signature, mapToNode1.signature ]));

    props.graph.layout();
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
            :graph="graph"
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
