<script setup lang="ts">
import { onMounted, onUnmounted, ref, shallowRef, computed } from 'vue';
import { type Graph, Edge, SelectionType, type Node } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import Warning from './Warning.vue';

const props = defineProps<{
    graph: Graph;
}>();

const emit = defineEmits<{
    (e: 'save'): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm', nodes: Node[], edges: Edge[]): void;
}>();

const nodes = shallowRef<Node[]>([]);
const edges = shallowRef<Edge[]>([]);
const confirmClicked = ref(false);
const showWarning = ref(false);
const warningMessage = ref('');

const nodesSelected = computed(() => nodes.value.some(node => !!node));
const noNodesSelected = computed(() => nodes.value.every(node => !node));

const isNodeTurn = computed(() => nodes.value.length === edges.value.length);

const selectedNodeLabels = computed(() => {
    const labels: string[] = [];
    for (let i = 0; i < nodes.value.length; i++) {
        labels.push(nodes.value[i].schemaObject.label);
        if (i < edges.value.length) {
            const edge = edges.value[i];
            const direction = edge.domainNode.equals(nodes.value[i]) ? '->' : '<-';
            labels.push(direction);
        }
    }
    return labels.join(' ');
});

function confirm() {
    confirmClicked.value = true;
    emit('confirm', nodes.value, edges.value);
}

function save() { // do not do anything, just go back to editor
    emit('cancel');
}

function cancel() {
    if (noNodesSelected.value) { // go back to editor
        emit('cancel');
    }
    
    nodes.value.forEach(node => node.unselect());
    nodes.value = []; 
    edges.value = [];

    if (confirmClicked.value) { // delete the edit (on BE)
        emit('cancel-edit');
        confirmClicked.value = false;
    }
}

const listener = props.graph.listen();

onMounted(() => {
    listener.onNode('tap', onNodeTapHandler);
    listener.onEdge('tap', onEdgeTapHandler);
});

onUnmounted(() => {
    listener.close();
});

function onNodeTapHandler(node: Node) {
    if (isNodeTurn.value) {
        nodes.value = [ ...nodes.value, node ];
        node.select({ type: SelectionType.Root, level: 0 });
        showWarning.value = false;
    }
    else {
        showWarning.value = true;
        warningMessage.value = 'Please select an edge next.';
    }
}

function onEdgeTapHandler(edge: Edge) {
    if (!isNodeTurn.value) {
        edges.value = [ ...edges.value, edge ];
        showWarning.value = false; // Hide warning on valid selection
    }
    else {
        showWarning.value = true;
        warningMessage.value = 'Please select a node next.';
    }
}
</script>

<template>
    <div class="recursion">
        <Warning 
            :show="showWarning"
            :message="warningMessage"
        />
        <h2>Find Recursion</h2>
        <ValueContainer>
            <ValueRow label="Recursive pattern:"> 
                {{ selectedNodeLabels }}
            </ValueRow>
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="!nodesSelected || confirmClicked"
                @click="confirm"
            >
                Confirm
            </button>
        </div>
        <div class="button-row">
            <button
                :disabled="!confirmClicked"
                @click="save"
            >
                Save
            </button>
            <button
                @click="cancel"
            >
                Cancel
            </button>
        </div>
    </div>
</template>

<style scoped>
.recursion {
    position: relative;
}
</style>