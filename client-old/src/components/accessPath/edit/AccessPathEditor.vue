<script setup lang="ts">
import { GraphComplexProperty, GraphSimpleProperty, GraphRootProperty } from '@/types/accessPath/graph';
import type { GraphChildProperty, GraphParentProperty } from '@/types/accessPath/graph/compositeTypes';
import { SelectionType, type Node, type Edge } from '@/types/categoryGraph';
import { shallowRef, ref, watch, computed, onMounted, onUnmounted } from 'vue';
import ParentPropertyDisplay from '../display/ParentPropertyDisplay.vue';
import type { Datasource } from '@/types/datasource';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import { ObjexIds, SignatureId, SignatureIdFactory, SpecialName, StaticName } from '@/types/identifiers';
import NodeInput from '@/components/input/NodeInput.vue';
import { useEvocat } from '@/utils/injects';
import EditProperty from './EditProperty.vue';
import AddProperty from './AddProperty.vue';
import PrimaryKeyInput from '../input/PrimaryKeyInput.vue';

/**
 * Enum for the different states of node selection.
 * - Default: No node selected
 * - OneNode: One node selected
 * - TwoNodes: Two nodes selected
 * - MultipleNodes: More than two nodes selected
 * - AddProperty: Add property
 * - EditProperty: Edit property
 */
enum State {
    Default, // user should select some nodes to create a kind
    OneNode,
    TwoNodes, 
    MultipleNodes,
    AddProperty,
    EditProperty,
}

/**
 * Generic state type that combines a state with an associated value.
 * @template State - The state type (e.g., Default, OneNode).
 * @template Value - The value type associated with the state.
 */
type GenericStateValue<State, Value> = { type: State } & Value;

/**
 * Type representing the different states and associated values in the component.
 */
type StateValue = GenericStateValue<State.Default, unknown> |
    GenericStateValue<State.OneNode, { node: Node }> |
    GenericStateValue<State.TwoNodes, { nodes: Node[] }> |
    GenericStateValue<State.MultipleNodes, { nodes: Node[] }> |
    GenericStateValue<State.AddProperty, { parent: GraphParentProperty }> |
    GenericStateValue<State.EditProperty, { property: GraphChildProperty }>;

/**
 * Props for the AccessPathEditor component.
 */
type AccessPathEditorProps = {
    /** The datasource object. */
    datasource: Datasource;
    /** The root property of the graph. */
    rootProperty: GraphRootProperty;
};

/**
 * Configuration for keyboard shortcuts used in the component.
 */
type ShortcutConfig = {
    /** Shortcut for inserting nodes. */
    insert?: {
        keyboard: string;
    };
    /** Shortcut for deleting nodes. */
    delete?: {
        keyboard: string;
    };
    /** Shortcut for setting the root node. */
    setRoot?: {
        keyboard: string;
    };
};

/**
 * Extracts the graph from Evocat.
 */
const { graph } = $(useEvocat());

/**
 * Props and event emitters for the component.
 */
const props = defineProps<AccessPathEditorProps>();
const emit = defineEmits([ 'finish', 'update:rootProperty', 'cancel' ]);

/**
 * Local state for root property and node selection.
 */
const localRootProperty = shallowRef<GraphRootProperty>(props.rootProperty);
const state = shallowRef<StateValue>({ type: State.Default });
const selectedNodes = shallowRef<Node[]>([]);
const supressStateUpdate = ref(false);

/**
 * Computed property to generate labels of selected nodes.
 */
const selectedNodeLabels = computed(() => selectedNodes.value.map(node => node?.metadata.label).join(', '));

/**
 * Local shortcut configuration loaded from external JSON.
 */
const shortcutConfig = ref<ShortcutConfig>({});

/**
 * Lifecycle hook to load shortcuts and set up keydown listener when the component is mounted.
 */
onMounted(async () => {
    await loadShortcuts();
    window.addEventListener('keydown', handleKeyDown);
});

/**
 * Lifecycle hook to remove the keydown listener when the component is unmounted.
 */
onUnmounted(() => {
    window.removeEventListener('keydown', handleKeyDown);
});

/**
 * Fetches keyboard shortcuts from the /shortcuts.json file.
 * @async
 */
async function loadShortcuts() {
    try {
        const response = await fetch('/shortcuts.json');
        if (response.ok) 
            shortcutConfig.value = await response.json();
        else 
            console.error('Failed to load shortcut config');
    }
    catch (error) {
        console.error('Error loading shortcuts:', error);
    }
}

/**
 * Handles keyboard shortcuts based on the current state of node selection.
 */
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

/**
 * Returns the primary key of the given objex IDs.
 */
function getInitialPrimaryKey(ids?: ObjexIds): SignatureId {
    if (!ids)
        return SignatureIdFactory.createEmpty();

    if (ids.isSignatures && ids.signatureIds.length > 0)
        return ids.signatureIds[0];

    return SignatureIdFactory.createEmpty();
}

const primaryKey = shallowRef(getInitialPrimaryKey(props.rootProperty.node.schemaObjex.ids));

/**
 * Requests to insert a node into the graph and update the root property.
 */
function insertRequested(node: Node) {
    if (insert(node))
        node.highlight();
    selectedNodes.value = [];
    emit('update:rootProperty', localRootProperty.value);
}

/**
 * Inserts a node into the graph and updates the parent property accordingly.
 */
function insert(node: Node): boolean {
    const children = graph.getChildrenForNode(node);
    const label = node.metadata.label.toLowerCase();
    //let parentProperty = parentNode ? getParentPropertyFromAccessPath(parentNode) : undefined;
    // assume the new property is connected to the root (parentProperty)
    const edges = graph.getEdges(node);
    const parentProperty = findMatchingProperty(edges, localRootProperty.value);

    if (!parentProperty) return false;

    const signature = graph.getSignature(node, parentProperty.node);

    let subpath: GraphChildProperty;
    if (children.length === 0)
        subpath = new GraphSimpleProperty(new StaticName(label), signature, parentProperty);
    else 
        subpath = new GraphComplexProperty(new StaticName(label), signature, parentProperty, []);

    parentProperty.updateOrAddSubpath(subpath);
    return true;
}

function findMatchingProperty(edges: Edge[], property: GraphRootProperty): GraphParentProperty | undefined {
    for (const edge of edges) {
        if (property.containsNode(edge.domainNode)) 
            return findPropertyForNode(property, edge.domainNode);        

        if (property.containsNode(edge.codomainNode)) 
            return findPropertyForNode(property, edge.codomainNode);        
    }

    return undefined;
}

function findPropertyForNode(property: GraphRootProperty, node: Node): GraphParentProperty | undefined {
    if (property.node.equals(node)) 
        return property;
    
    for (const subpath of property.subpaths) {
        if (subpath instanceof GraphComplexProperty || subpath instanceof GraphRootProperty) {
            const result = findPropertyInSubpaths(subpath, node);
            if (result) return result;
        }
    }

    return undefined;
}

function findPropertyInSubpaths(property: GraphParentProperty, node: Node): GraphParentProperty | undefined {
    if (property.node.equals(node)) 
        return property;    

    if (property instanceof GraphComplexProperty) {
        for (const subpath of property.subpaths) {
            if (subpath instanceof GraphComplexProperty || subpath instanceof GraphRootProperty) {
                const result = findPropertyInSubpaths(subpath, node);
                if (result) return result;
            }
        }
    }

    return undefined;
}

/**
 * Finds a subpath corresponding to a node within the root property.
 */
function findSubpathForNode(node: Node): GraphChildProperty | undefined {
    return searchSubpathsForNode(props.rootProperty, node) as GraphChildProperty | undefined;
}

/**
 * Searches through subpaths of a property to find a node.
 */
function searchSubpathsForNode(property: GraphParentProperty, node: Node): GraphParentProperty | undefined {
    if (property.node.equals(node)) return property;

    if (property instanceof GraphComplexProperty) {
        for (const subpath of property.subpaths) {
            if (subpath instanceof GraphComplexProperty) {
                const result = searchSubpathsForNode(subpath, node);
                if (result) return result;
            }
        }
    }
}

function insertBetweenClicked() {
    console.log('Inserting between');
}

/**
 * Deletes the requested nodes from the access path and updates the root property.
 */
function deleteRequested(nodes: Node[]) {
    nodes.forEach(node => {
        if (isNodeInAccessPath(node)) {
            node.unhighlight();
            props.rootProperty.unhighlightPath();
            props.rootProperty.removeSubpathForNode(node);
            props.rootProperty.highlightPath();
        }
    });

    selectedNodes.value = [];
    emit('update:rootProperty', localRootProperty.value);
}

function cancelSelection() {
    selectedNodes.value = [];
}

/**
 * Sets the selected node as the root property and updates the root access path.
 */
function setRootRequested(node: Node) {
    const label = node.metadata.label.toLowerCase();

    // FIXME StaticName.fromString(label)

    const newRoot = new GraphRootProperty(new SpecialName('root'), node);

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

    const ids = node.schemaObjex?.ids;
    primaryKey.value = getInitialPrimaryKey(ids);

    node.unselect();
    selectedNodes.value = [];
    emit('update:rootProperty', newRoot);
    localRootProperty.value = newRoot;
}

function editPropertyClicked(property: GraphChildProperty) {
    supressStateUpdate.value = true;
    state.value = {
        type: State.EditProperty,
        property,
    };
}

function addPropertyClicked(parentProperty: GraphComplexProperty) {
    supressStateUpdate.value = true;
    state.value = {
        type: State.AddProperty,
        parent: parentProperty,
    };
}

function setStateToDefault() {
    supressStateUpdate.value = false;
    state.value = { type: State.Default };
    emit('update:rootProperty', props.rootProperty);
}

const kindName = shallowRef('');

/**
 * Finishes the mapping process by emitting the finish event with the primary key and root property.
 */
function finishMapping() {
    emit('finish', primaryKey.value, props.rootProperty, kindName.value);
}

/**
 * Watches the selectedNodes array and updates the state based on the number of valid nodes selected.
 */
watch(selectedNodes, (nodes) => {
    if (supressStateUpdate.value)
        return;

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

/**
 * Checks if a node is part of the access path.
 */
function isNodeInAccessPath(node: Node): boolean {
    return localRootProperty.value.containsNode(node) ?? false;
}

/**
 * Emits the cancel event to notify the parent component of the cancellation.
 */
function cancel() {
    emit('cancel');
}

const showSelectedNodes = computed(() => {
    return (
        state.value.type !== State.Default &&
        state.value.type !== State.AddProperty &&
        state.value.type !== State.EditProperty
    );
});

</script>

<template>
    <div>
        <div>
            <div class="editor">
                <template v-if="showSelectedNodes">
                    <ValueRow label="Selected Nodes:">
                        {{ selectedNodeLabels }}
                    </ValueRow>
                </template>
                <template v-if="state.type === State.Default">
                    <h2 class="custom-text">
                        Select valid nodes to edit
                    </h2>

                    <div style="display: grid; grid-template-columns: auto auto; gap: 0px 8px;">
                        <div>
                            Datasource:
                        </div>
                        <div>
                            {{ datasource.label }}
                        </div>

                        <div>
                            Root object:
                        </div>
                        <div>
                            {{ rootProperty.node.label }}
                        </div>

                        <div>
                            Kind name:
                        </div>
                        <div>
                            <input
                                v-model="kindName"
                                style="height: 24px;"
                            />
                        </div>

                        <template v-if="rootProperty.node.schemaObjex.ids">
                            <div>
                                Primary key:
                            </div>
                            <div>
                                <PrimaryKeyInput
                                    v-model="primaryKey"
                                    :ids="rootProperty.node.schemaObjex.ids"
                                />
                            </div>
                        </template>
                    </div>

                    <div class="button-row">
                        <button
                            @click="finishMapping"
                        >
                            Finish mapping
                        </button>
                        <!-- 
                        Don't want to accidentally cancel the mapping process ...
                        <button
                            @click="cancel"
                        >
                            Cancel
                        </button> -->
                    </div>
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
                        <button @click="cancelSelection">
                            Cancel
                        </button>
                    </div>
                </template>
                <template v-if="state.type === State.TwoNodes">
                    <div class="options">
                        <button 
                            :disabled="true"
                            @click="insertBetweenClicked"
                        >
                            Insert Between
                        </button>
                        <button @click="deleteRequested(state.nodes)">
                            Delete
                        </button>
                        <button @click="cancelSelection">
                            Cancel
                        </button>
                    </div>
                </template>
                <template v-if="state.type === State.MultipleNodes">
                    <div class="options">
                        <button @click="deleteRequested(state.nodes)">
                            Delete
                        </button>
                        <button @click="cancelSelection">
                            Cancel
                        </button>
                    </div>
                </template>
                <template v-if="state.type === State.AddProperty">
                    <AddProperty
                        :datasource="props.datasource"
                        :parent-property="state.parent"
                        @save="setStateToDefault"
                        @cancel="setStateToDefault"
                    />
                </template>
                <template v-if="state.type === State.EditProperty">
                    <EditProperty
                        :datasource="props.datasource"
                        :property="state.property"
                        @save="setStateToDefault"
                        @cancel="setStateToDefault"
                    />
                </template>
            </div>
        </div>
        <NodeInput
            :model-value="selectedNodes"
            :type="SelectionType.Selected" 
            @update:model-value="selectedNodes = $event"
        />
        <ParentPropertyDisplay
            :property="localRootProperty"
            @complex:click="editPropertyClicked"
            @simple:click="editPropertyClicked"
            @add:click="addPropertyClicked"
        />
        <div class="shortcuts-box">
            <table class="shortcut-table">
                <thead>
                    <tr>
                        <th>Operation</th>
                        <th>Keyboard Shortcut</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>Insert</td>
                        <td>{{ shortcutConfig.insert?.keyboard }}</td>
                    </tr>
                    <tr>
                        <td>Delete</td>
                        <td>{{ shortcutConfig.delete?.keyboard }}</td>
                    </tr>
                    <tr>
                        <td>Set Root</td>
                        <td>{{ shortcutConfig.setRoot?.keyboard }}</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<style scoped>

.shortcuts-box {
    position: fixed;
    bottom: 0;
    right: 0;
    padding: 10px;
    font-size: 0.85rem;
    color: rgb(134, 133, 133);
    background-color: #f9f9f9;
    border-top: 1px solid #ccc;
    text-align: left;
    box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1);
}

.shortcuts-box p {
    margin: 0 0 5px 0;
    font-weight: bold;
}

.shortcut-table {
    width: 100%;
    border-collapse: collapse;
    text-align: left;
}

.shortcut-table th, .shortcut-table td {
    padding: 8px;
    border: 1px solid #ddd; /* Border for the table */
}

.shortcut-table th {
    background-color: #f2f2f2;
    font-weight: bold;
    font-size: 12px;
}

.shortcut-table td {
    font-weight: normal;
    font-size: 12px;
}

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