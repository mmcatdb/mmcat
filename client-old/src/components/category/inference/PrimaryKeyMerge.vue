<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { type Graph, SelectionType, type Node } from '@/types/categoryGraph';
import type { Candidates, PrimaryKeyCandidate } from '@/types/inference/candidates';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';
import Message from './Message.vue';
import Divider from '@/components/layout/Divider.vue';

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** The graph object used for selecting nodes. */
    graph: Graph;
    /** The candidates available for primary key merging. */
    candidates: Candidates;
}>();

/**
 * Emits custom events to the parent component.
 */
const emit = defineEmits<{
    (e: 'confirm', payload: Node[] | PrimaryKeyCandidate): void;
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
 * Reactive reference for tracking which candidates have been clicked.
 */
const clickedCandidates = ref<PrimaryKeyCandidate[]>([]);

/**
 * Computed property to check if two nodes have been selected.
 */
const nodesSelected = computed(() => !!nodes.value[0] && !!nodes.value[1]);

/**
 * Computed property to check if no nodes are selected.
 */
const noNodesSelected = computed(() => !nodes.value[0] && !nodes.value[1]);

/**
 * Reactive reference to control the visibility of the warning message.
 */
const showMessage = ref(false);

/**
 * Reactive reference to hold the warning message text.
 */
const messageText = ref('');

/**
 * Confirms the selected primary key candidate and emits the 'confirm' event.
 */
function confirmCandidate(candidate: PrimaryKeyCandidate) {
    if (!clickedCandidates.value.includes(candidate)) {
        clickedCandidates.value.push(candidate);
        messageText.value = 'Candidate applied. Save to confirm.';
        showMessage.value = true;
    }

    confirmClicked.value = true;
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
 * Cancels the current operation and goes back to the editor.
 * Emits the 'cancel' event.
 */
function save() {
    showMessage.value = false;
    emit('cancel');
}

/**
 * Cancels the current selection or edit.
 * If no nodes are selected and confirm button has not been clicked, it goes back to the editor.
 * Otherwise, it emits the 'cancel-edit' event.
 */
function cancel() {
    if (noNodesSelected.value && !confirmClicked.value) 
        emit('cancel');    
    
    nodes.value = [];  // Unselect selected nodes.

    if (confirmClicked.value) {
        emit('cancel-edit');
        confirmClicked.value = false;
    }

    showMessage.value = false;
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
    <div class="position-relative">
        <Message 
            :show="showMessage"
            :message="messageText"
        />
        <div class="mb-2 d-flex gap-4">
            <label class="d-flex align-items-center cursor-pointer">
                <input
                    v-model="inputType"
                    type="radio"
                    value="manual"
                />
                Manual
            </label>
            <label class="d-flex align-items-center cursor-pointer">
                <input
                    v-model="inputType"
                    type="radio"
                    value="candidate"
                />
                Candidate
            </label>
        </div>

        <p>
            First select the object with the primary key, then the object identified by it.
        </p>

        <ValueContainer v-if="inputType === 'manual'">
            <ValueRow label="Primary Key object:"> 
                {{ nodes[0]?.metadata.label }}
            </ValueRow>
            <ValueRow label="Identified object:"> 
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
            <div v-if="props.candidates.pkCandidates.length > 0">
                <p>
                    Select from the discovered candidates:
                </p>

                <button
                    v-for="(candidate, index) in props.candidates.pkCandidates"
                    :key="'pk-' + index"
                    class="candidate-button"
                    :disabled="confirmClicked"
                    :class="{ 'clicked': clickedCandidates.includes(candidate) }"
                    @click="confirmCandidate(candidate)"
                >
                    <div class="candidate-content">
                        <div class="candidate-side pk-label">
                            <strong>PK</strong>
                        </div>
                        <div class="candidate-side candidate-text">
                            <div>{{ splitName(candidate.hierarchicalName).partA }}</div>
                            <div>{{ splitName(candidate.hierarchicalName).partB }}</div>
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

.pk-label {
    margin-right: 10px;
    font-weight: bold;
}

.candidate-text {
    display: flex;
    flex-direction: column;
    text-align: left;
    justify-content: center;
}
</style>
