<script setup lang="ts">
import { onMounted, onUnmounted, ref, shallowRef, computed } from 'vue';
import { type Graph, Edge, SelectionType, type Node } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import Warning from './Warning.vue';

/**
 * Props passed to the component.
 * @typedef {Object} Props
 * @property {Graph} graph - The graph object used for recursion analysis.
 */
const props = defineProps<{
    graph: Graph;
}>();

/**
 * Emits custom events to the parent component.
 * @emits save - Emitted when the user saves the changes.
 * @emits cancel - Emitted when the user cancels the current operation.
 * @emits cancel-edit - Emitted when the user cancels the current edit.
 * @emits confirm - Emitted when the user confirms the recursion pattern.
 * @param {Node[]} nodes - The selected nodes for the recursion.
 * @param {Edge[]} edges - The selected edges for the recursion.
 */
const emit = defineEmits<{
    (e: 'save'): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm', nodes: Node[], edges: Edge[]): void;
}>();

/**
 * Reactive reference for storing selected nodes.
 */
const nodes = shallowRef<Node[]>([]);

/**
 * Reactive reference for storing selected edges.
 */
const edges = shallowRef<Edge[]>([]);

/**
 * Tracks whether the confirm button has been clicked.
 */
const confirmClicked = ref(false);

/**
 * Tracks whether the warning message should be shown.
 */
const showWarning = ref(false);

/**
 * Stores the warning message to be displayed.
 */
const warningMessage = ref('');

/**
 * Computed property to check if any nodes are selected.
 */
const nodesSelected = computed(() => nodes.value.some(node => !!node));

/**
 * Computed property to check if no nodes are selected.
 */
const noNodesSelected = computed(() => nodes.value.every(node => !node));

/**
 * Computed property to determine if it's a node's turn to be selected (based on equal numbers of nodes and edges).
 */
const isNodeTurn = computed(() => nodes.value.length === edges.value.length);

/**
 * Computed property that returns a string of selected node labels, with arrows indicating the direction of edges between them.
 */
const selectedNodeLabels = computed(() => {
    const labels: string[] = [];
    for (let i = 0; i < nodes.value.length; i++) {
        labels.push(nodes.value[i].metadata.label);
        if (i < edges.value.length) {
            const edge = edges.value[i];
            const direction = edge.domainNode.equals(nodes.value[i]) ? '->' : '<-';
            labels.push(direction);
        }
    }
    return labels.join(' ');
});

/**
 * Confirms the selected recursion pattern and emits the 'confirm' event.
 */
function confirm() {
    confirmClicked.value = true;
    emit('confirm', nodes.value, edges.value);
}

/**
 * Saves the current state and emits the 'cancel' event (goes back to the editor without making changes).
 */
function save() {
    emit('cancel');
}

/**
 * Cancels the current operation and resets the selected nodes and edges.
 * If no nodes are selected, it goes back to the editor by emitting the 'cancel' event.
 * Otherwise, it unselects nodes and edges and emits the 'cancel-edit' event.
 */
function cancel() {
    showWarning.value = false;

    if (noNodesSelected.value) 
        emit('cancel');    
    
    nodes.value.forEach(node => node.unselect());
    nodes.value = [];
    edges.value = [];

    if (confirmClicked.value) {
        emit('cancel-edit');
        confirmClicked.value = false;
    }
}

/**
 * Listener for graph interactions.
 */
const listener = props.graph.listen();

/**
 * Mounts event listeners for node and edge tap events on component mount.
 */
onMounted(() => {
    listener.onNode('tap', onNodeTapHandler);
    listener.onEdge('tap', onEdgeTapHandler);
});

/**
 * Removes event listeners on component unmount.
 */
onUnmounted(() => {
    listener.close();
});

/**
 * Handles node tap events.
 * If it's a node's turn to be selected, the node is added to the selected list.
 * Otherwise, a warning message is displayed.
 * @param {Node} node - The tapped node.
 */
function onNodeTapHandler(node: Node) {
    if (isNodeTurn.value) {
        nodes.value = [...nodes.value, node];
        node.select({ type: SelectionType.Root, level: 0 });
        showWarning.value = false;
    } else {
        showWarning.value = true;
        warningMessage.value = 'Please select an edge next.';
    }
}

/**
 * Handles edge tap events.
 * If it's an edge's turn to be selected, the edge is added to the selected list.
 * Otherwise, a warning message is displayed.
 * @param {Edge} edge - The tapped edge.
 */
function onEdgeTapHandler(edge: Edge) {
    if (!isNodeTurn.value) {
        edges.value = [...edges.value, edge];
        showWarning.value = false;
    } else {
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
