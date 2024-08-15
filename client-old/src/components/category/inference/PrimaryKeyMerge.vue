<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { type Graph, SelectionType, type Node } from '@/types/categoryGraph';
import { Candidates, PrimaryKeyCandidate } from '@/types/inference/candidates';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';

const props = defineProps<{
    graph: Graph;
    candidates: Candidates;
}>();

const emit = defineEmits<{
    (e: 'confirm', payload: Node[] | PrimaryKeyCandidate): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
}>();

const inputType = ref<'manual' | 'candidate'>('manual');

const nodes = shallowRef<(Node)[]>([]);
const confirmClicked = ref(false);
const clickedCandidates = ref<PrimaryKeyCandidate[]>([]);

const nodeSelected = computed(() => !!nodes.value[0]);
const noNodeSelected = computed(() => !nodes.value[0]);

function confirmCandidate(candidate: PrimaryKeyCandidate) {
    if (!clickedCandidates.value.includes(candidate)) 
        clickedCandidates.value.push(candidate);

    confirmClicked.value = true;
    emit('confirm', candidate);
}

function confirmNodes() {
    confirmClicked.value = true;
    emit('confirm', nodes.value as Node[]);
}

function save() { // do not do anything, just go back t editor
    emit('cancel');
}

function cancel() {
    if (noNodeSelected.value) { // go back to editor
        emit('cancel');
    }
    
    nodes.value = [ undefined ];  //unselect selected nodes

    if (confirmClicked.value) { // delete the edit (on BE)
        emit('cancel-edit');
        confirmClicked.value = false;
    }
}

function splitName(name: string) {
    const [ partA, partB ] = name.split('/');
    return { partA, partB };
}

</script>

<template>
    <div class="primaryKeyMerge">
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
            <ValueRow label="Primary Key object:"> 
                {{ nodes[0]?.schemaObject.label }}
            </ValueRow>
            <NodeInput
                v-model="nodes"
                :graph="props.graph"
                :count="1"
                :type="SelectionType.Selected"
            />
        </ValueContainer>
        <div v-else>
            <div v-if="props.candidates.pkCandidates.length > 0">
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
                :disabled="!nodeSelected || confirmClicked"
                @click="confirmNodes"
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
