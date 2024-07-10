<script setup lang="ts">
import { ref, shallowRef, watch, onMounted, onUnmounted } from 'vue';
import { computed } from '@vue/reactivity';
import { Graph } from '@/types/categoryGraph';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { SelectionType, type Node, type TemporaryEdge } from '@/types/categoryGraph';
import NodeInput from '@/components/input/NodeInput.vue';

const props = defineProps<{
    graph: Graph
}>();

const emit = defineEmits<{
    (e: 'save'): void;
    (e: 'cancel'): void;
    (e: 'confirm', nodes: (Node | undefined)[]): void;
}>();

const nodes = shallowRef<(Node | undefined)[]>([]);
const confirmClicked = ref(false);

const nodesSelected = computed(() => !!nodes.value[0] && !!nodes.value[1]);

// chci poslat upravy na BE a dostat zmenennou SK
function confirm() {
    confirmClicked.value = true;
    console.log("confirm in merge");
    emit('confirm', nodes.value);
}

function save() {
    // here I want to save the change to the job.data
    emit('save');
}

function cancel() {
    emit('cancel');
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
                :disabled="!nodesSelected"
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
        </div>
        <div class="button-row">
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
