<script setup lang="ts">
import { Edge, SelectionType, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import { computed, onUnmounted, ref, shallowRef, watch } from 'vue';
import MinimumInput from './MinimumInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';
import { useEvocat } from '@/utils/injects';

const { evocat, graph } = $(useEvocat());

type UpdateMorphismProps = {
    edge: Edge;
};

const props = defineProps<UpdateMorphismProps>();

const emit = defineEmits([ 'save', 'cancel' ]);

const nodes = shallowRef<(Node | undefined)[]>([ props.edge.domainNode, props.edge.codomainNode ]);
const temporayEdge = ref<TemporaryEdge>();

const label = ref(props.edge.metadata.label);
const min = ref(props.edge.schemaMorphism.min);

const nodesSelected = computed(() => !!nodes.value[0] && !!nodes.value[1]);
const changed = computed(() =>
    !props.edge.domainNode.equals(nodes.value[0])
        || !props.edge.codomainNode.equals(nodes.value[1])
        || props.edge.metadata.label !== label.value.trim()
        || props.edge.schemaMorphism.min !== min.value,
);
const isNew = computed(() => props.edge.schemaMorphism.isNew);

defineExpose({ changed });

watch(nodes, (newValue, oldValue) => {
    if (oldValue && newValue[0] === oldValue[0] && newValue[1] === oldValue[1])
        return;

    temporayEdge.value?.delete();

    if (!newValue[0] || !newValue[1])
        return;

    if (props.edge.domainNode.equals(newValue[0]) && props.edge.codomainNode.equals(newValue[1]))
        return;

    temporayEdge.value = graph.createTemporaryEdge(newValue[0], newValue[1]);
}, { immediate: true });

onUnmounted(() => {
    temporayEdge.value?.delete();
});

function save() {
    const [ node1, node2 ] = nodes.value;
    if (!node1 || !node2)
        return;

    // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (only if the cardinality changed).

    const old = props.edge.schemaMorphism;
    const update = {
        dom: node1.schemaObjex,
        cod: node2.schemaObjex,
        min: min.value,
        label: label.value.trim(),
        tags: old.tags,
    };
    evocat.updateMorphism(old, update);

    temporayEdge.value?.delete();

    graph.layout();
    emit('save');
}

function cancel() {
    emit('cancel');
}

function deleteFunction() {
    evocat.deleteMorphism(props.edge.schemaMorphism);

    emit('save');
}

function switchNodes() {
    nodes.value = [ nodes.value[1], nodes.value[0] ];
}
</script>

<template>
    <div class="add-morphism">
        <h2>Edit Schema Morphism</h2>
        <ValueContainer>
            <ValueRow label="Domain obje">
                {{ nodes[0]?.metadata.label }}
            </ValueRow>
            <ValueRow label="Codomain ob">
                {{ nodes[1]?.metadata.label }}
            </ValueRow>
            <ValueRow label="Label?:">
                <input v-model="label" />
            </ValueRow>
            <ValueRow label="Signature:">
                {{ edge.schemaMorphism.signature }}
            </ValueRow>
            <MinimumInput
                v-model="min"
            />
            <!--  :disabled="!isNew"  -->
        </ValueContainer>
        <NodeInput
            v-model="nodes"
            :count="2"
            :type="SelectionType.Selected"
        />
        <!--  :disabled="!isNew"  -->
        <div class="button-row">
            <button
                :disabled="!nodesSelected || !changed"
                @click="save"
            >
                <!--  v-if="isNew"  -->
                Confirm
            </button>
            <button
                :disabled="!nodesSelected"
                @click="switchNodes"
            >
                <!--  v-if="isNew"  -->
                Switch
            </button>
            <button @click="cancel">
                Cancel
            </button>
            <button
                @click="deleteFunction"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style scoped>
.number-input {
    max-width: 80px;
}
</style>

