<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { SelectionType, type Node, type Graph } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** The graph object used in the NodeInput component. */
    graph: Graph;
}>();

/**
 * Emits custom events to the parent component.
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
    
    
    nodes.value = [];  // unselect selected nodes

    if (confirmClicked.value) {
        emit('cancel-edit');
        confirmClicked.value = false;
    }
}

</script>

<template>
    <div class="position-relative">
        <h3 class="cluster-title">
            Cluster Objects
            <span class="tooltip-container">
                <span class="question-mark">?</span>
                <span class="tooltip-text">
                Select objects that form a cluster — they should have the same structure and share part of their name.<br>
                For example, leaf nodes like <code>color_red</code>, <code>color_blue</code>, and <code>color_yellow</code> can form a cluster because they follow the same naming pattern and structure.
                </span>
            </span> 
        </h3>
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

<style>
.cluster-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tooltip-container {
  position: relative;
  display: inline-block;
  flex-shrink: 0;
}

.question-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  font-size: 11px;
  border-radius: 50%;
  background-color: #999;
  color: white;
  font-weight: bold;
  cursor: default;
}

.tooltip-text {
  visibility: hidden;
  opacity: 0;
  position: absolute;
  bottom: 125%;
  left: 50%;
  transform: translateX(-50%);
  width: max-content;
  max-width: 220px;
  padding: 6px 8px;
  font-size: 12px;
  background-color: #333;
  color: #fff;
  text-align: left;
  border-radius: 6px;
  z-index: 1;
  pointer-events: none;
  transition: opacity 0.2s ease-in-out;
}

.tooltip-container:hover .tooltip-text {
  visibility: visible;
  opacity: 1;
}
</style>
