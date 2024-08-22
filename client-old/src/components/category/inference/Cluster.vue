<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue';
import { SelectionType, type Node, type Graph } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';

const props = defineProps<{
    graph: Graph;
}>();

const emit = defineEmits<{
    (e: 'save'): void;
    (e: 'cancel'): void;
    (e: 'cancel-edit'): void;
    (e: 'confirm', nodes: Node[]): void;
}>();

const nodes = shallowRef<(Node)[]>([]);
const confirmClicked = ref(false);

const nodesSelected = computed(() => nodes.value.some(node => !!node));
const noNodesSelected = computed(() => nodes.value.every(node => !node));

const selectedNodeLabels = computed(() => {
    return nodes.value
        .filter(node => node !== undefined)
        .map(node => node?.schemaObject.label)
        .join(', ');
});

function confirm() {
    confirmClicked.value = true;
    emit('confirm', nodes.value as Node[]);
}

function save() { // do not do anything, just go back to editor
    emit('cancel');
}

function cancel() {
    if (noNodesSelected.value) { // go back to editor
        emit('cancel');
    }
    

    nodes.value = [undefined, undefined];  //unselect selected nodes

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