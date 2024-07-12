<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { Graph } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { SelectionType, type Node } from '@/types/categoryGraph';
import NodeInput from '@/components/input/NodeInput.vue';

const props = defineProps<{
    graph: Graph
}>();

const emit = defineEmits<{
    (e: 'save'): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm', nodes: (Node | undefined)[]): void;
}>();

const nodes = shallowRef<(Node | undefined)[]>([]);
const confirmClicked = ref(false);

const nodesSelected = computed(() => !!nodes.value[0] && !!nodes.value[1]);
const noNodesSelected = computed(() => !nodes.value[0] && !nodes.value[1]);

function confirm() {
    confirmClicked.value = true;
    emit('confirm', nodes.value);
}

function save() { // do not do anything, just go back t editor
    emit('cancel');
}

// TODO: when nodes selected but not confirmed and cancel clicked I want it to remain in Merge, and only unselect the selected nodes
function cancel() {
    if (noNodesSelected) { // go back to editor
        emit('cancel');
    }
    
    nodes.value = [undefined, undefined];  //unselect selected nodes

    if (confirmClicked) { // delete the edit (on BE)
        emit('cancel-edit');
    }
}

</script>

<template>
       <div class="merge">
        <h2>Merge Objects</h2>
        <ValueContainer>
            <ValueRow label="Reference object:"> 
                {{ nodes[0]?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Referred object:"> 
                {{ nodes[1]?.schemaObject.label }}
            </ValueRow>
        </ValueContainer>
        <NodeInput
            v-model="nodes"
            :graph="props.graph"
            :count="2"
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

<style>
.number-input {
    max-width: 80px;
}
.button-row {
    display: flex;
    gap: 10px;
    justify-content: center;
}
</style>
