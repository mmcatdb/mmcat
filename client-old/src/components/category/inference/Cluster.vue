<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { SelectionType, type Node, type Graph } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';

/**
 * Props passed to the component.
 * @typedef {Object} Props
 * @property {Graph} graph - The graph object used in the NodeInput component.
 */
const props = defineProps<{
    graph: Graph;
}>();

/**
 * Emits custom events to the parent component.
 * @emits save - Triggered when the "Save" button is clicked.
 * @emits cancel - Triggered when the "Cancel" button is clicked.
 * @emits cancel-edit - Triggered when the "Cancel" button is clicked after confirmation.
 * @emits confirm - Triggered when the "Confirm" button is clicked with the selected nodes.
 * @param {Node[]} nodes - Array of selected nodes emitted with the "confirm" event.
 */
const emit = defineEmits<{
    (e: 'save'): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm', nodes: Node[]): void;
}>();

/**
 * Stores the selected nodes in a shallow reference.
 */
const nodes = shallowRef<(Node)[]>([]);

/**
 * Tracks whether the confirm button has been clicked.
 */
const confirmClicked = ref(false);

/**
 * Computed property that checks if any nodes have been selected.
 */
const nodesSelected = computed(() => nodes.value.some(node => !!node));

/**
 * Computed property that checks if no nodes are selected.
 */
const noNodesSelected = computed(() => nodes.value.every(node => !node));

/**
 * Computed property that returns a string of labels of the selected nodes.
 */
const selectedNodeLabels = computed(() => {
    return nodes.value
        .filter(node => node !== undefined)
        .map(node => node?.metadata.label)
        .join(', ');
});

/**
 * Confirms the selected nodes and emits the "confirm" event.
 * Sets confirmClicked to true.
 */
function confirm() {
    confirmClicked.value = true;
    emit('confirm', nodes.value as Node[]);
}

/**
 * Emits the "cancel" event without any additional action.
 */
function save() {
    emit('cancel');
}

/**
 * Cancels the current selection and emits either "cancel" or "cancel-edit" based on the state.
 * If no nodes are selected, it emits "cancel". Otherwise, it resets the selected nodes and emits "cancel-edit" if confirmClicked was true.
 */
function cancel() {
    if (noNodesSelected.value) 
        emit('cancel');
    
    
    nodes.value = [ undefined, undefined ];  // unselect selected nodes

    if (confirmClicked.value) {
        emit('cancel-edit');
        confirmClicked.value = false;
    }
}

</script>

<template>
    <div class="cluster">
        <h2>Cluster Objects</h2>
        <ValueContainer>
            <ValueRow label="Objects forming a cluster:"> 
                {{ selectedNodeLabels }}
            </ValueRow>
        </ValueContainer>
        <NodeInput
            v-model="nodes"
            :graph="props.graph"
            :type="SelectionType.Selected"
        />
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
.cluster {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.button-row {
    display: flex;
    gap: 10px;
    justify-content: center;
}
</style>
