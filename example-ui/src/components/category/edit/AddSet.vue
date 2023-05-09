<script setup lang="ts">
import { onUnmounted, ref, watch } from 'vue';
import { SelectionType, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality } from '@/types/schema';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { SignatureId } from '@/types/identifiers';
import NodeInput from '@/components/input/NodeInput.vue';
import { useEvocat } from '@/utils/injects';

const evocat = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const setLabel = ref('');
const nodes = ref<(Node | undefined)[]>([]);
const temporayEdge = ref<TemporaryEdge | null>(null);

watch(nodes, (newValue, oldValue) => {
    if (newValue[0] === oldValue[0] && newValue[1] === oldValue[1])
        return;

    temporayEdge.value?.delete();

    if (!newValue[0] || !newValue[1])
        return;

    temporayEdge.value = evocat.graph.createTemporaryEdge(newValue[0], newValue[1]);
});

onUnmounted(() => {
    temporayEdge.value?.delete();
});

function save() {
    const [ node1, node2 ] = nodes.value;
    if (!node1 || !node2)
        return;

    const setObject = evocat.graph.schemaCategory.createObject(setLabel.value);
    const setNode = evocat.graph.createNode(setObject, 'new');

    const setToNode1 = evocat.graph.schemaCategory.createMorphism(setObject, node1.schemaObject, Cardinality.One, '#role');
    evocat.graph.createEdge(setToNode1, 'new');
    const setToNode2 = evocat.graph.schemaCategory.createMorphism(setObject, node2.schemaObject, Cardinality.One, '#role');
    evocat.graph.createEdge(setToNode2, 'new');

    setNode.addSignatureId(new SignatureId([ setToNode1.signature, setToNode2.signature ]));

    evocat.graph.layout();
    emit('save');
}

function cancel() {
    emit('cancel');
}
</script>

<template>
    <div>
        <h2>Add Set</h2>
        <ValueContainer>
            <ValueRow label="Label:">
                <input v-model="setLabel" />
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
                :disabled="!setLabel || !nodes[0] || !nodes[1]"
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
