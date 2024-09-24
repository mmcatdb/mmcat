<script setup lang="ts">
import type { GraphComplexProperty, GraphRootProperty, GraphChildProperty, GraphParentProperty } from '@/types/accessPath/graph';
import { SelectionType, type Node } from '@/types/categoryGraph';
import { shallowRef, ref, watch } from 'vue';
import ParentPropertyDisplay from '../display/ParentPropertyDisplay.vue';
import type { Datasource } from '@/types/datasource';
import StaticNameInput from '../input/StaticNameInput.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import PrimaryKeyInput from '../input/PrimaryKeyInput.vue';
import { ObjectIds, SignatureId, SignatureIdFactory } from '@/types/identifiers';
import NodeInput from '@/components/input/NodeInput.vue';
import { isKeyPressed, Key } from '@/utils/keyboardInput';

enum State {
    Default, // user should select some nodes to create a kind
    OneNode,
    TwoNodes, 
    MultipleNodes
}

type GenericStateValue<State, Value> = { type: State } & Value;

type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.OneNode, { node: Node }> |
    GenericStateValue<State.TwoNodes, { nodes: Node[] }> |
    GenericStateValue<State.MultipleNodes, { nodes: Node[] }>;
    

type AccessPathEditorProps = {
    datasource: Datasource;
    rootProperty: GraphRootProperty;
};

const props = defineProps<AccessPathEditorProps>();

const emit = defineEmits([ 'finish' ]);

const state = shallowRef<StateValue>({ type: State.Default });
const selectedNodes = ref<Node[]>([]);

function setStateToDefault() {
    state.value = { type: State.Default };
}

function insertClicked() {
    console.log('Inserting node:', state.value);
}

function deleteClicked() {
    console.log('Deleting nodes:', selectedNodes.value);
    selectedNodes.value = []; // Clear the selection after deletion
}

function setRootClicked() {
    if (state.value.type === State.OneNode) {
        console.log('Setting root to node:', state.value.node);
    }
}

function renameClicked() {
    if (state.value.type === State.OneNode) {
        console.log('Renaming node:', state.value.node);
    }
}

function finishMapping() {
    emit('finish', primaryKey.value);
}

function onNodeClick(node: Node) {
    console.log("Am I here?");
    const index = selectedNodes.value.indexOf(node);

    if (index !== -1) {
        // If the node is already selected, unselect it and remove it from the selected nodes
        node.unselect();
        selectedNodes.value.splice(index, 1);
    } else {
        // If the node is not selected, add it to the selection
        selectedNodes.value.push(node);
        node.select({ type: SelectionType.Root, level: selectedNodes.value.length - 1 });
    }

    // Reassign selectedNodes.value to trigger reactivity
    console.log(selectedNodes);
    console.log("changing the nodes");
    selectedNodes.value = [...selectedNodes.value];  // This ensures Vue detects the changes
}


watch(selectedNodes, (nodes) => {
    if (nodes.length === 0) 
        state.value = { type: State.Default };
    else if (nodes.length === 1) 
        state.value = { type: State.OneNode, node: nodes[0] as Node };
    else if (nodes.length === 2) 
        state.value = { type: State.TwoNodes, nodes: nodes as Node[] };
    else 
        state.value = { type: State.MultipleNodes, nodes: nodes as Node[] };    
});

</script>

<template>
    <div class="divide">
        <div>
            <div class="editor">
                <template v-if="state.type === State.Default">
                    <h2 class="custom-text">Select Nodes to edit</h2>
                    <div class="button-row">
                        <button
                            @click="finishMapping"
                        >
                            Finish mapping
                        </button>
                    </div>
                </template>
                <template v-else-if="state.type === State.OneNode">
                    <div class="options">
                        <button @click="insertClicked">
                            Insert
                        </button>
                        <button @click="deleteClicked">
                            Delete
                        </button>
                        <button @click="setRootClicked">
                            Set Root
                        </button>
                        <button @click="renameClicked">
                            Rename
                        </button>
                    </div>
                </template>
                <template v-else-if="state.type === State.TwoNodes">
                    <div class="options">
                        <button @click="insertClicked">
                            Insert Between
                        </button>
                        <button @click="deleteClicked">
                            Delete
                        </button>
                    </div>
                </template>
                <template v-else-if="state.type === State.MultipleNodes">
                    <div class="options">
                        <button @click="deleteClicked">
                            Delete
                        </button>
                    </div>
                </template>
            </div>
        </div>
        <NodeInput
            :model-value="selectedNodes"
            :type="SelectionType.Selected"
            @node-click="onNodeClick" 
            @update:modelValue="selectedNodes = $event"
        />
        <ParentPropertyDisplay
            :property="rootProperty"
        />
    </div>
</template>

<style scoped>
.accessPathInput {
    color: white;
    background-color: black;
    width: 600px;
    height: 600px;
    font-size: 15px;
}

.createProperty {
    padding: 16px;
    margin: 16px;
    border: 1px solid white;
}

.options {
    display: flex;
    flex-direction: column;
}

.options button {
    margin-top: 6px;
    margin-bottom: 6px;
}

.options button:first-of-type {
    margin-top: 0px;
}

.options button:last-of-type {
    margin-bottom: 0px;
}

.custom-text {
    font-size: 24px;  
    font-style: italic;
    color: #494848;
    text-align: center;
}

</style>