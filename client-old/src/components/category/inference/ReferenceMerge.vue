<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { type Graph, SelectionType, type Node } from '@/types/categoryGraph';
import type { Candidates, ReferenceCandidate } from '@/types/inference/candidates';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** The graph object used for selecting nodes. */
    graph: Graph;
    /** The candidates available for reference merging. */
    candidates: Candidates;
}>();

/**
 * Emits custom events to the parent component.
 */
const emit = defineEmits<{
    (e: 'confirm', payload: Node[] | ReferenceCandidate): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
}>();

/**
 * Reactive reference for tracking whether the user is inputting nodes manually or selecting a candidate.
 */
const inputType = ref<'manual' | 'candidate'>('manual');

/**
 * Reactive reference for storing selected nodes.
 */
const nodes = shallowRef<(Node)[]>([]);

/**
 * Reactive reference for tracking whether the confirm button has been clicked.
 */
const confirmClicked = ref(false);

/**
 * Reactive reference for tracking clicked reference candidates.
 */
const clickedCandidates = ref<ReferenceCandidate[]>([]);

/**
 * Reactive reference for tracking the index of the clicked candidate.
 */
const clickedIndex = ref<number | undefined>(undefined);

/**
 * Computed property to check if two nodes have been selected.
 */
const nodesSelected = computed(() => !!nodes.value[0] && !!nodes.value[1]);

/**
 * Computed property to check if no nodes are selected.
 */
const noNodesSelected = computed(() => !nodes.value[0] && !nodes.value[1]);

/**
 * Confirms the selected reference candidate and emits the 'confirm' event.
 */
function confirmCandidate(candidate: ReferenceCandidate, index: number) {
    if (!clickedCandidates.value.includes(candidate)) 
        clickedCandidates.value.push(candidate);

    confirmClicked.value = true;
    clickedIndex.value = index;
    emit('confirm', candidate);
}

/**
 * Confirms the selected nodes and emits the 'confirm' event.
 */
function confirmNodes() {
    confirmClicked.value = true;
    emit('confirm', nodes.value as Node[]);
}

/**
 * Cancels the current operation and goes back to the editor without making changes.
 * Emits the 'cancel' event.
 */
function save() {
    emit('cancel');
}

/**
 * Cancels the current selection or edit.
 * If no nodes are selected and the confirm button has not been clicked, it goes back to the editor.
 * Otherwise, it unselects nodes and resets the edit state, emitting the 'cancel-edit' event.
 */
function cancel() {
    if (noNodesSelected.value && !confirmClicked.value) { // go back to editor
        emit('cancel');
    }
    
    nodes.value = [];  // Unselect selected nodes.

    if (confirmClicked.value) {
        emit('cancel-edit');
        confirmClicked.value = false;
        clickedIndex.value = undefined;  // Optionally reset clicked index.
    }
}

/**
 * Splits the hierarchical name of a candidate into two parts.
 */
function splitName(name: string) {
    const [ partA, partB ] = name.split('/');
    return { partA, partB };
}

</script>

<template>
    <div class="referenceMerge">
        <div class="input-type">
            <label class="radio-label">
                <input
                    v-model="inputType"
                    type="radio"
                    value="manual"
                /> Manual
            </label>
            <label class="radio-label">
                <input
                    v-model="inputType"
                    type="radio"
                    value="candidate"
                /> Candidate
            </label>
        </div>
        <ValueContainer v-if="inputType === 'manual'">
            <ValueRow label="Reference object:"> 
                {{ nodes[0]?.metadata.label }}
            </ValueRow>
            <ValueRow label="Referred object:"> 
                {{ nodes[1]?.metadata.label }}
            </ValueRow>
            <NodeInput
                v-model="nodes"
                :graph="props.graph"
                :count="2"
                :type="SelectionType.Selected"
            />
        </ValueContainer>
        <div v-else>
            <div v-if="props.candidates.refCandidates.length > 0">
                <button
                    v-for="(candidate, index) in props.candidates.refCandidates"
                    :key="'ref-' + index"
                    class="candidate-button"
                    :disabled="clickedIndex !== undefined && clickedIndex !== index"
                    :class="{ 'clicked': clickedCandidates.includes(candidate) }"
                    @click="confirmCandidate(candidate, index)"
                >
                    <div class="candidate-content">
                        <div class="candidate-side">
                            <div>{{ splitName(candidate.referencing).partA }}</div>
                            <div>{{ splitName(candidate.referencing).partB }}</div>
                        </div>
                        <div class="candidate-middle">
                            REF
                        </div>
                        <div class="candidate-side">
                            <div>{{ splitName(candidate.referred).partA }}</div>
                            <div>{{ splitName(candidate.referred).partB }}</div>
                        </div>
                    </div>
                </button>
            </div>
            <p v-else>
                No candidates available
            </p>
        </div>
        <div class="button-row">
            <button
                v-if="inputType === 'manual'"
                :disabled="!nodesSelected || confirmClicked"
                @click="confirmNodes"
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

<style scoped>
.radio-label {
    margin-right: 20px;
    cursor: pointer;
} 

.candidate-button {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    padding: 10px;
    margin-bottom: 10px;
    text-align: left;
    background-color: #f5f5f5;
    border: 1px solid #ccc;
    border-radius: 4px;
    cursor: pointer;
}

.candidate-button.clicked {
    background-color: #d3e2ff;
    border-color: #007bff;
}

.candidate-button:hover:not(.clicked) {
    background-color: #e0e0e0;
}

@media (prefers-color-scheme: dark) {
    .candidate-button {
        background-color: #2a2a2a; /* Dark background */
        border-color: #444; /* Dark border */
        color: #f5f5f5; /* Light text color for readability */
    }

    .candidate-button.clicked {
        background-color: #3b5998; /* Slightly highlighted dark color */
        border-color: #2d4373;
    }

    .candidate-button:hover:not(.clicked) {
        background-color: #444; /* Hover color for dark mode */
    }
}

.candidate-content {
    display: flex;
    width: 100%;
    justify-content: space-between;
    align-items: center;
}

.candidate-side {
    display: flex;
    flex-direction: column;
    margin-right: 10px;
    text-align: center;
}

.candidate-middle {
    font-weight: bold;
    margin: 0 10px;
}
</style>
