<script setup lang="ts">
import { computed, ref, shallowRef } from 'vue';
import { GraphRootProperty, GraphSimpleProperty, GraphComplexProperty } from '@/types/accessPath/graph';
import type { GraphChildProperty, GraphParentProperty } from '@/types/accessPath/graph/compositeTypes';
import { SignatureId, StaticName } from '@/types/identifiers';
import { type Node, SelectionType } from '@/types/categoryGraph';
import AccessPathEditor from './edit/AccessPathEditor.vue';
import { useEvocat } from '@/utils/injects';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import NodeInput from '@/components/input/NodeInput.vue';
import type { Datasource } from '@/types/datasource';

/**
 * Extracts the graph object from Evocat.
 */
const { graph } = $(useEvocat());

/**
 * Props passed to the component.
 */
const props = defineProps<{
    /** The selected datasource. */
    selectedDatasource: Datasource;
}>();

const accessPath = ref<GraphRootProperty>();
const nodes = shallowRef<(Node)[]>([]);
const selectingRootNode = ref<Node>();
const selectingOriginalRootNode = ref<Node>();
//const selectedNodes = ref<Node[]>([]);
const selectedNodes = shallowRef<Node[]>([]);
const rootConfirmed = ref(false);

const isConfirmButtonDisabled = computed(() => {
    return !props.selectedDatasource || nodes.value.length < 2;
});

/**
 * Stores the previous parent property used for path construction.
 */
let previousParentProperty: GraphParentProperty;

/**
 * A set to track processed node keys.
 */
let processedNodes = new Set<number>();

/**
 * Computed property that generates a string of the labels for the selected nodes.
 */
const selectedNodeLabels = computed(() => selectedNodes.value.map(node => node?.metadata.label).join(', '));

const kindName = ref<string | undefined>(undefined);

/**
 * Emits custom events to the parent component.
 */
const emit = defineEmits([ 'finish', 'cancel' ]);

/**
 * Confirms the selected datasource and root node. It marks the root node as selected and finalizes the root.
 */
function confirmDatasourceAndRootNode() {
    if (!props.selectedDatasource || nodes.value.length < 2)
        return;

    console.log(nodes.value[0]);
    console.log(nodes.value[1]);

    selectingOriginalRootNode.value = nodes.value[0];
    selectingRootNode.value = nodes.value[1];

    nodes.value[0].unselect();
    nodes.value[1].unselect();

    selectingRootNode.value.becomeRoot();
    rootConfirmed.value = true;
}

/**
 * Confirms the selected nodes and constructs the access path from the root node and the selected nodes.
 */
function confirmSelectedNodes() {
    if (!props.selectedDatasource || !selectingRootNode.value) return;

    if (selectingOriginalRootNode.value && !selectingRootNode.value.equals(selectingOriginalRootNode.value)) {
        const trueLabel = selectingOriginalRootNode.value.metadata.label.toLowerCase();
        accessPath.value = new GraphRootProperty(StaticName.fromString(trueLabel), selectingOriginalRootNode.value);
        processNode(selectingRootNode.value);
        kindName.value = selectingRootNode.value.metadata.label;
    } else {
        accessPath.value = new GraphRootProperty(StaticName.fromString(selectingRootNode.value.metadata.label.toLowerCase()), selectingRootNode.value);
    }
    
    if (selectedNodes.value.length !== 0) {
        selectedNodes.value.forEach(node => processNode(node));
        processedNodes.clear();
    }

    accessPath.value?.highlightPath();
}

/**
 * Processes a single node by creating its subpath and adding it to the access path.
 */
function processNode(node: Node) {
    if (!processedNodes.has(node.schemaObjex.key.value)) {
        const subpath = createSubpathForNode(node);
        if (subpath) {
            accessPath.value?.updateOrAddSubpath(subpath);        
            processedNodes.add(node.schemaObjex.key.value);
        }
    }
}

/**
 * Creates a subpath for the given node based on its children and parent property.
 */
function createSubpathForNode(node: Node): GraphChildProperty | undefined {
    if (!graph) {
        console.error('Graph instance is not available.');
        return;
    }

    const children = filterChildren(node);
    //const children = graph.getChildrenForNode(node);
    //const parentNode = graph.getParentNode(node);
    const parentNode = accessPath.value?.node;

    if (!parentNode) return;

    const signature = graph.getSignature(node, parentNode);
    const label = node.metadata.label.toLowerCase();
    let parentProperty = parentNode ? getParentPropertyFromAccessPath(parentNode) ?? previousParentProperty : previousParentProperty;

    if (!parentProperty) return;

    let subpath: GraphChildProperty;
    if (graph.getChildrenForNode(node).length === 0) 
        subpath = new GraphSimpleProperty(StaticName.fromString(label), signature, parentProperty);
    else 
        subpath = new GraphComplexProperty(StaticName.fromString(label), signature, parentProperty, []);

    if (subpath instanceof GraphComplexProperty) {
        previousParentProperty = subpath;
        const childSubpaths = children.map(child => createSubpathForNode(child));
        childSubpaths.forEach(childSubpath => {
            if (childSubpath && subpath instanceof GraphComplexProperty) 
                subpath.updateOrAddSubpath(childSubpath);            
        });
    }

    processedNodes.add(node.schemaObjex.key.value);
    return subpath;
}

/**
 * Filters the children nodes of the given node to only include those that are selected.
 */
function filterChildren(node: Node): Node[] {
    if (graph) {
        const allChildren = graph.getChildrenForNode(node);
        return allChildren.filter(child => 
            selectedNodes.value.some(selectedNode => selectedNode.equals(child)) &&
            !processedNodes.has(child.schemaObjex.key.value),
        );
    }
    return [];
}

/**
 * Retrieves the parent property from the access path for the specified parent node.
 */
function getParentPropertyFromAccessPath(parentNode: Node): GraphParentProperty | undefined {
    return accessPath.value ? searchSubpathsForNode(accessPath.value, parentNode) : undefined;
}

/**
 * Searches for the node within the subpaths of a given property.
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


/**
 * Updates the root property with a new root property and highlights the path.
 */
function updateRootProperty(newRootProperty: GraphRootProperty) {
    undoAccessPath();
    newRootProperty.node.becomeRoot();
    accessPath.value = newRootProperty;
    accessPath.value.highlightPath();
}

/**
 * Resets the access path and clears root node status and highlights.
 */
function undoAccessPath() {
    rootConfirmed.value = false;
    accessPath.value?.node.removeRoot();
    accessPath.value?.unhighlightPath();
    selectingRootNode.value?.unhighlight();
    selectingRootNode.value?.removeRoot();
}

/**
 * Emits the finish event with the primary key and the access path, signaling the end of the mapping process.
 */
function createMapping(primaryKey: SignatureId) {
    emit('finish', primaryKey, accessPath.value, kindName.value);
}

/**
 * Cancels the current operation, resets the access path, and emits the cancel event.
 */
function cancel() {
    undoAccessPath();
    emit('cancel');
}
 
</script>

<template>
    <div class="divide">
        <div>
            <div
                v-if="!accessPath || !selectedDatasource"
                class="editor"
            >
                <ValueContainer v-if="!rootConfirmed">
                    <ValueRow label="Original Root object:">
                        {{ nodes[0]?.metadata.label }}
                    </ValueRow>
                    <ValueRow label="Root object:">
                        {{ nodes[1]?.metadata.label }}
                    </ValueRow>
                    <NodeInput
                        v-model="nodes"
                        :graph="graph"
                        :count="2"
                        :type="SelectionType.Selected"                    
                    />
                </ValueContainer>    
                <div
                    v-if="!rootConfirmed"
                    class="button-row"
                >
                    <button
                        :disabled="isConfirmButtonDisabled"
                        @click="confirmDatasourceAndRootNode"
                    >
                        Confirm
                    </button>
                    <button
                        @click="cancel"
                    >
                        Cancel
                    </button>
                </div>
                <ValueContainer v-if="rootConfirmed">
                    <ValueRow label="AccessPath objects:">
                        <div
                            class="d-flex flex-wrap"
                            style="width: 300px"
                        >
                            {{ selectedNodeLabels }}
                            <NodeInput
                                :graph="graph"
                                :model-value="selectedNodes"
                                :type="SelectionType.Selected"
                                @update:modelValue="selectedNodes = $event"
                            />
                        </div>
                    </ValueRow>
                </ValueContainer>
                <div 
                    v-if="rootConfirmed" 
                    class="button-row"
                >
                    <button
                        @click="confirmSelectedNodes"
                    >
                        Confirm
                    </button>
                    <button
                        @click="cancel"
                    >
                        Cancel
                    </button>
                </div>
            </div>
            <AccessPathEditor
                v-else
                :datasource="selectedDatasource"
                :root-property="accessPath"
                @finish="createMapping"
                @update:rootProperty="updateRootProperty"
                @cancel="cancel"
            />
        </div>
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

.editor {
    display: flex;
    flex-direction: column;
}

.display {
    padding: 16px;
    margin: 16px;
}

.createProperty {
    padding: 16px;
    margin: 16px;
    border: 1px solid white;
}
</style>
