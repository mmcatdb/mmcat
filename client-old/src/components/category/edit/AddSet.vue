<script setup lang="ts">
import { onUnmounted, ref, shallowRef, watch } from 'vue';
import { SelectionType, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { Cardinality } from '@/types/schema';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';
import { useEvocat } from '@/utils/injects';
import { ObjectIds } from '@/types/identifiers';

const { evocat, graph } = $(useEvocat());

const emit = defineEmits([ 'save', 'cancel' ]);

const setLabel = ref('');
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
    evocat.compositeOperation('addSet', () => {
        const [ node1, node2 ] = nodes.value;
        if (!node1 || !node2 || !node1.schemaObjex.ids || !node2.schemaObjex.ids)
            return;

        const setObject = evocat.createObjex({
            label: setLabel.value,
        });

        const setToNode1 = evocat.createMorphism({
            domKey: setObject.key,
            codKey: node1.schemaObjex.key,
            min: Cardinality.One,
            label: '#role',
        });

        const setToNode2 = evocat.createMorphism({
            domKey: setObject.key,
            codKey: node2.schemaObjex.key,
            min: Cardinality.One,
            label: '#role',
        });

        evocat.updateObjex(setObject, {
            ids: ObjectIds.createCrossProduct([
                { signature: setToNode1.signature, ids: node1.schemaObjex.ids },
                { signature: setToNode2.signature, ids: node2.schemaObjex.ids },
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
        <h2>Add Set</h2>
        <ValueContainer>
            <ValueRow label="Label:">
                <input v-model="setLabel" />
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
