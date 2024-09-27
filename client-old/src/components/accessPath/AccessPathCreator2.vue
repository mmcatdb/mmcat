<script setup lang="ts">
import { computed, onMounted, provide, ref, shallowRef } from 'vue';
import { GraphRootProperty, GraphSimpleProperty, GraphComplexProperty } from '@/types/accessPath/graph';
import type { GraphChildProperty, GraphParentProperty } from '@/types/accessPath/graph/compositeTypes';
import { SignatureId, StaticName } from '@/types/identifiers';
import { type Node, type Graph, SelectionType } from '@/types/categoryGraph';
import AccessPathEditor2 from './edit/AccessPathEditor2.vue';
import { LogicalModel } from '@/types/logicalModel';
import { useSchemaCategoryInfo, useSchemaCategoryId, evocatKey, type EvocatContext, useEvocat } from '@/utils/injects';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import SingleNodeInput from '@/components/input/SingleNodeInput.vue';
import NodeInput from '@/components/input/NodeInput.vue';
import { isKeyPressed, Key } from '@/utils/keyboardInput';

const { graph } = $(useEvocat());

const props = defineProps<{
    selectedLogicalModel: LogicalModel;
}>();

const accessPath = ref<GraphRootProperty>();
const selectingRootNode = ref<Node>();
const selectedNodes = ref<Node[]>([]);
const rootConfirmed = ref(false);

let previousParentProperty: GraphParentProperty;
let processedNodes = new Set<number>();

const selectedNodeLabels = computed(() => selectedNodes.value.map(node => node?.metadata.label).join(', '));

const emit = defineEmits([ 'finish' ]);

function confirmDatasourceAndRootNode() {
    if (!props.selectedLogicalModel || !selectingRootNode.value)
        return;

    selectingRootNode.value.unselect();
    selectingRootNode.value.becomeRoot();
    rootConfirmed.value = true;
}

function confirmSelectedNodes() {
    if (!props.selectedLogicalModel || !selectingRootNode.value) return;

    const label = selectingRootNode.value.metadata.label.toLowerCase();
    accessPath.value = new GraphRootProperty(StaticName.fromString(label), selectingRootNode.value);
    
    if (selectedNodes.value.length !== 0) {
        selectedNodes.value.forEach(node => processNode(node));
        processedNodes.clear();
    }

    accessPath.value?.highlightPath();
}

function processNode(node: Node) {
    if (!processedNodes.has(node.schemaObject.key.value)) {
        const subpath = createSubpathForNode(node);
        if (subpath) {
            accessPath.value?.updateOrAddSubpath(subpath);        
            processedNodes.add(node.schemaObject.key.value);
        }
    }
}

function createSubpathForNode(node: Node): GraphChildProperty | undefined {
    if (!graph) {
        console.error('Graph instance is not available.');
        return;
    }

    const children = filterChildren(node);
    const parentNode = graph.getParentNode(node);
    const signature = graph.getSignature(node, parentNode);
    const label = node.metadata.label.toLowerCase(); // why to lower case though?
    let parentProperty = parentNode ? getParentPropertyFromAccessPath(parentNode) ?? previousParentProperty : previousParentProperty;

    if (!parentProperty) return;

    let subpath: GraphChildProperty;
    if (children.length === 0) 
        subpath = new GraphSimpleProperty(StaticName.fromString(label), signature, parentProperty);
    else 
        subpath = new GraphComplexProperty(StaticName.fromString(label), signature, parentProperty, []);

    if (subpath instanceof GraphComplexProperty) {
        previousParentProperty = subpath;
        const childSubpaths = children.map(child => createSubpathForNode(child));
        childSubpaths.forEach(childSubpath => {
            if (childSubpath) 
                subpath.updateOrAddSubpath(childSubpath);            
        });
    }

    processedNodes.add(node.schemaObject.key.value);
    return subpath;
}

function filterChildren(node: Node): Node[] {
    if (graph) {
        const allChildren = graph.getChildrenForNode(node);
        return allChildren.filter(child => 
            selectedNodes.value.some(selectedNode => selectedNode.equals(child)) &&
            !processedNodes.has(child.schemaObject.key.value)
        );
    }
    return [];
}

function getParentPropertyFromAccessPath(parentNode: Node): GraphParentProperty | undefined {
    return accessPath.value ? searchSubpathsForNode(accessPath.value, parentNode) : undefined;
}

function searchSubpathsForNode(property: GraphParentProperty, node: Node): GraphParentProperty | undefined {
    if (property.node.equals(node)) return property;

    if (property instanceof GraphComplexProperty) {
        for (const subpath of property.subpaths) {
            const result = searchSubpathsForNode(subpath, node);
            if (result) return result;            
        }
    }
}

function onNodeTapHandler(node: Node) {
    if (!rootConfirmed.value) return;

    if (isKeyPressed(Key.Shift)) {
        const currentLength = selectedNodes.value.length;
        selectedNodes.value = selectedNodes.value.filter(n => !n.equals(node));
        if (selectedNodes.value.length < currentLength) {
            node.unselect();
        } else {
            selectedNodes.value.push(node);
            node.select({ type: SelectionType.Selected, level: 0 });
        }
    } else {
        selectedNodes.value.forEach(n => n.unselect());
        selectedNodes.value = [ node ];
        node.select({ type: SelectionType.Selected, level: 0 });
    }
}

function updateRootProperty(newRootProperty: GraphRootProperty) {
    accessPath.value?.node.removeRoot();
    accessPath.value?.unhighlightPath();

    newRootProperty.node.becomeRoot();
    accessPath.value = newRootProperty;
    accessPath.value.highlightPath();
}

function createMapping(primaryKey: SignatureId) {
    emit('finish', primaryKey, accessPath);
}
</script>

<template>
    <div class="divide">
        <div>
            <div
                v-if="!accessPath || !selectedLogicalModel"
                class="editor"
            >
                <ValueContainer v-if="!rootConfirmed">
                    <ValueRow label="Root object:">
                        <SingleNodeInput
                            v-model="selectingRootNode"                                
                            :type="SelectionType.Root"
                        />
                    </ValueRow>
                </ValueContainer>
                <div v-if="!rootConfirmed" class="button-row">
                    <button
                        :disabled="!selectedLogicalModel || !selectingRootNode || rootConfirmed"
                        @click="confirmDatasourceAndRootNode"
                    >
                        Confirm Root Node
                    </button>
                </div>
                <ValueContainer v-if="rootConfirmed">
                    <ValueRow label="AccessPath objects:">
                        {{ selectedNodeLabels }}
                        <NodeInput
                            :graph="graph"
                            :model-value="selectedNodes"
                            :type="SelectionType.Selected"
                            @update:modelValue="selectedNodes = $event"
                        />
                    </ValueRow>
                </ValueContainer>
                <div v-if="rootConfirmed" class="button-row">
                    <button
                        @click="confirmSelectedNodes"
                    >
                        Confirm Selected Nodes
                    </button>
                </div>
            </div>
            <AccessPathEditor2
                v-else
                :datasource="selectedLogicalModel.datasource"
                :root-property="accessPath"
                @finish="createMapping"
                @update:rootProperty="updateRootProperty"
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
