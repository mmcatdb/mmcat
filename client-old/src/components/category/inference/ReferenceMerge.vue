<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { type Graph, SelectionType, type Node } from '@/types/categoryGraph';
import { Candidates, ReferenceCandidate } from '@/types/inference/candidates';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';

const props = defineProps<{
    graph: Graph;
    candidates: Candidates;
}>();

const emit = defineEmits<{
    (e: 'confirm', nodes: Node[]): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
}>();

const inputType = ref<'manual' | 'candidate'>('manual');

const nodes = shallowRef<(Node)[]>([]);
const confirmClicked = ref(false);

const nodesSelected = computed(() => !!nodes.value[0] && !!nodes.value[1]);
const noNodesSelected = computed(() => !nodes.value[0] && !nodes.value[1]);

function selectCandidate(candidate: ReferenceCandidate) {
    //emit('select-candidate', candidate);
    // Here you can add additional logic if needed
}

function confirm() {
    confirmClicked.value = true;
    emit('confirm', nodes.value as Node[]);
}

function save() { // do not do anything, just go back t editor
    emit('cancel');
}

function cancel() {
    if (noNodesSelected.value) { // go back to editor
        emit('cancel');
    }
    
    nodes.value = [ undefined, undefined ];  // unselect selected nodes

    if (confirmClicked.value) { // delete the edit (on BE)
        emit('cancel-edit');
        confirmClicked.value = false;
    }
}

function splitName(name: string) {
    const [partA, partB] = name.split('/');
    return { partA, partB };
}
//TODO: the NodeInput component used to be outside of the ValueContainer, make sure it works this way

</script>

<template>
    <div class="referenceMerge">
        <div class="input-type">
            <label>
                <input
                    v-model="inputType"
                    type="radio"
                    value="manual"
                /> Manual
            </label>
            <label>
                <input
                    v-model="inputType"
                    type="radio"
                    value="candidate"
                /> Candidate
            </label>
        </div>
        <ValueContainer v-if="inputType === 'manual'">
            <ValueRow label="Reference object:"> 
                {{ nodes[0]?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Referred object:"> 
                {{ nodes[1]?.schemaObject.label }}
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
                    @click="selectCandidate(candidate)"
                    class="candidate-button"
                >
                    <div class="candidate-content">
                        <div class="candidate-side">
                            <div>{{ splitName(candidate.referencing).partA }}</div>
                            <div>{{ splitName(candidate.referencing).partB }}</div>
                        </div>
                        <div class="candidate-middle">REF</div>
                        <div class="candidate-side">
                            <div>{{ splitName(candidate.referred).partA }}</div>
                            <div>{{ splitName(candidate.referred).partB }}</div>
                        </div>
                    </div>
                </button>
            </div>
            <p v-else>No candidates available</p>
        </div>
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

.candidate-button:hover {
    background-color: #e0e0e0;
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