<script setup lang="ts">
import { GraphComplexProperty, GraphSimpleProperty, GraphRootProperty } from '@/types/accessPath/graph';
import type { GraphChildProperty, GraphParentProperty } from '@/types/accessPath/graph/compositeTypes';
import { SelectionType, type Node } from '@/types/categoryGraph';
import { shallowRef, ref, watch, computed, onMounted, onUnmounted} from 'vue';
import ParentPropertyDisplay from '../display/ParentPropertyDisplay.vue';
import type { Datasource } from '@/types/datasource';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { ObjectIds, SignatureId, SignatureIdFactory, StaticName } from '@/types/identifiers';
import NodeInput from '@/components/input/NodeInput.vue';
import { useEvocat } from '@/utils/injects';

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

type ShortcutConfig = {
    insert?: {
        keyboard: string;
    };
    delete?: {
        keyboard: string;
    };
    setRoot?: {
        keyboard: string;
    };
};

const { graph } = $(useEvocat());

const props = defineProps<AccessPathEditorProps>();

const emit = defineEmits([ 'finish', 'update:rootProperty', 'cancel' ]);

const localRootProperty = shallowRef<GraphRootProperty>(props.rootProperty);
const state = shallowRef<StateValue>({ type: State.Default });
const selectedNodes = ref<Node[]>([]);
const selectedNodeLabels = computed(() => selectedNodes.value.map(node => node?.metadata.label).join(', '));
const shortcutConfig = ref<ShortcutConfig>({});

onMounted(async () => {
    await loadShortcuts();
    window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
    window.removeEventListener('keydown', handleKeyDown);
});

async function loadShortcuts() {
    try {
        const response = await fetch('/shortcuts.json');
        if (response.ok) 
            shortcutConfig.value = await response.json();
        else 
            console.error('Failed to load shortcut config');        
    } catch (error) {
        console.error('Error loading shortcuts:', error);
    }
}

function handleKeyDown(event: KeyboardEvent) {
    const key = event.key.toLowerCase(); // normalize it
    
    const insertShortcut = shortcutConfig.value.insert?.keyboard?.toLowerCase();
    const deleteShortcut = shortcutConfig.value.delete?.keyboard?.toLowerCase();
    const setRootShortcut = shortcutConfig.value.setRoot?.keyboard?.toLowerCase();
    
    if (key === insertShortcut && state.value.type === State.OneNode) 
        insertRequested(state.value.node);  
    else if (key === deleteShortcut && state.value.type === State.OneNode)
        deleteRequested([ state.value.node ]);
    else if (key === deleteShortcut && (state.value.type === State.TwoNodes || state.value.type === State.MultipleNodes))
        deleteRequested(state.value.nodes);  
    else if (key === setRootShortcut && state.value.type === State.OneNode)
        setRootRequested(state.value.node);      
}

function getInitialPrimaryKey(ids?: ObjectIds): SignatureId {
    if (!ids)
        return SignatureIdFactory.createEmpty();

    if (ids.isSignatures && ids.signatureIds.length > 0)
        return ids.signatureIds[0];

    return SignatureIdFactory.createEmpty();
}

const primaryKey = shallowRef(getInitialPrimaryKey(props.rootProperty.node.schemaObject.ids));

function insertRequested(node: Node) {
    if (insert(node))
        node.highlight();
    selectedNodes.value = [];
    emit('update:rootProperty', localRootProperty.value);
}

function insert(node: Node): boolean {
    const children = graph.getChildrenForNode(node);
    const parentNode = graph.getParentNode(node);
    const label = node.metadata.label.toLowerCase();
    let parentProperty = parentNode ? getParentPropertyFromAccessPath(parentNode) : undefined;

    if (!parentProperty || !parentNode) return false;

    const signature = graph.getSignature(node, parentNode);

    let subpath: GraphChildProperty;
    if (children.length === 0)
        subpath = new GraphSimpleProperty(StaticName.fromString(label), signature, parentProperty);
    else 
        subpath = new GraphComplexProperty(StaticName.fromString(label), signature, parentProperty, []);

    parentProperty.updateOrAddSubpath(subpath);
    localRootProperty.value = parentProperty as GraphRootProperty;
    return true;
}

function getParentPropertyFromAccessPath(parentNode: Node): GraphParentProperty | undefined {
    return props.rootProperty ? searchSubpathsForNode(props.rootProperty, parentNode) : undefined;
}

function findSubpathForNode(node: Node): GraphChildProperty | undefined {
    return searchSubpathsForNode(props.rootProperty, node) as GraphChildProperty | undefined;
}

function searchSubpathsForNode(property: GraphParentProperty, node: Node): GraphParentProperty | undefined {
    if (property.node.equals(node)) return property;

    if (property instanceof GraphComplexProperty || property instanceof GraphRootProperty) {
        for (const subpath of property.subpaths) {
            const result = searchSubpathsForNode(subpath, node);
            if (result) return result;            
        }
    }
}

function insertBetweenClicked() {
    console.log('Inserting betweenxx');
}

function deleteRequested(nodes: Node[]) {
    nodes.forEach(node => {
        if (isNodeInAccessPath(node)) {
            node.unhighlight();
            props.rootProperty.removeSubpathForNode(node);   
        }
    });
    selectedNodes.value = [];
    emit('update:rootProperty', localRootProperty.value);
}

function setRootRequested(node: Node) {
    const label = node.metadata.label.toLowerCase();
    const newRoot = new GraphRootProperty(StaticName.fromString(label), node);

    if (!isNodeInAccessPath(node)) {
        if (!insert(node)) {
            node.unselect();
            selectedNodes.value = [];
            emit('update:rootProperty', newRoot);
            return;
        }   
    }
    const newSubpaths = findSubpathForNode(node);
    if (newSubpaths) 
        newRoot.updateOrAddSubpath(newSubpaths);    
    
    node.unselect();
    selectedNodes.value = [];
    emit('update:rootProperty', newRoot);
    localRootProperty.value = newRoot;
}


function renameClicked() {
    if (state.value.type === State.OneNode) 
        console.log('Renaming node:', state.value.node);    
}

function finishMapping() {
    emit('finish', primaryKey.value, props.rootProperty);
}

watch(selectedNodes, (nodes) => {
    const validNodes = nodes.filter((node): node is Node => node !== undefined);
    if (validNodes.length === 0) 
        state.value = { type: State.Default };
    else if (validNodes.length === 1) 
        state.value = { type: State.OneNode, node: validNodes[0] as Node };
    else if (validNodes.length === 2) 
        state.value = { type: State.TwoNodes, nodes: validNodes as Node[] };
    else 
        state.value = { type: State.MultipleNodes, nodes: validNodes as Node[] };    
});

function isNodeInAccessPath(node: Node): boolean {
    return localRootProperty.value.containsNode(node) ?? false;
}

function cancel() {
    emit('cancel');
}

</script>

<template>
    <div class="divide">
        <div>
            <div class="editor">
                <template v-if="state.type !== State.Default">
                    <ValueRow label="Selected Nodes:">
                        {{ selectedNodeLabels }}
                    </ValueRow>
                </template>
                <template v-if="state.type === State.Default">
                    <h2 class="custom-text">
                        Select valid nodes to edit
                    </h2>
                    <div class="button-row">
                        <button
                            @click="finishMapping"
                        >
                            Finish mapping
                        </button>
                    </div>
                    <button
                        @click="cancel"
                    >
                        Cancel
                    </button>
                </template>
                <template v-if="state.type === State.OneNode">
                    <div class="options">
                        <button 
                            v-if="!isNodeInAccessPath(state.node)"
                            @click="insertRequested(state.node)"
                        >
                            Insert
                        </button>
                        <button
                            v-if="isNodeInAccessPath(state.node)"
                            @click="deleteRequested([state.node])"
                        >
                            Delete
                        </button>
                        <button @click="setRootRequested(state.node)">
                            Set Root
                        </button>
                        <button 
                            v-if="isNodeInAccessPath(state.node)"
                            @click="renameClicked"
                        >
                            Rename
                        </button>
                    </div>
                </template>
                <template v-if="state.type === State.TwoNodes">
                    <div class="options">
                        <button @click="insertBetweenClicked">
                            Insert Between
                        </button>
                        <button @click="deleteRequested(state.nodes)">
                            Delete
                        </button>
                    </div>
                </template>
                <template v-if="state.type === State.MultipleNodes">
                    <div class="options">
                        <button @click="deleteRequested(state.nodes)">
                            Delete
                        </button>
                    </div>
                </template>
            </div>
        </div>
        <NodeInput
            :model-value="selectedNodes"
            :type="SelectionType.Selected" 
            @update:modelValue="selectedNodes = $event"
        />
        <ParentPropertyDisplay
            :property="localRootProperty"
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