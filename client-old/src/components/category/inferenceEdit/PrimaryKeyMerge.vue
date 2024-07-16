<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { Graph } from '@/types/categoryGraph';
import { SelectionType, type Node } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';

const props = defineProps<{
    graph: Graph
}>();

const emit = defineEmits<{
    (e: 'save'): void;
    (e: 'confirm', nodes: (Node | undefined)[]): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
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

function cancel() {
    if (noNodesSelected.value) { // go back to editor
        emit('cancel');
    }
    
    nodes.value = [undefined, undefined];  //unselect selected nodes

    if (confirmClicked.value) { // delete the edit (on BE)
        console.log("cancelling edit");
        emit('cancel-edit');
    }
}

</script>

<template>
    <div class="referenceMerge">
        <ValueContainer>
            <ValueRow label="Primary Key Root object:"> 
                {{ nodes[0]?.schemaObject.label }}
            </ValueRow>
            <ValueRow label="Primary Key object:"> 
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