<script setup lang="ts">
import { shallowRef, watch, ref } from 'vue';
import type { Job } from '@/types/job';
import type { Graph, Node, Edge } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import type { Category } from '@/types/schema';
import EditorForInferenceSchemaCategory from './EditorForInferenceSchemaCategory.vue';
import LayoutSelector from './LayoutSelector.vue';
import type { LayoutType } from '@/types/inference/layoutType';
import { type InferenceEdit, RecursionInferenceEdit, ClusterInferenceEdit, PrimaryKeyMergeInferenceEdit, ReferenceMergeInferenceEdit, PatternSegment } from '@/types/inference/inferenceEdit'; 
import { Candidates, ReferenceCandidate, PrimaryKeyCandidate } from '@/types/inference/candidates'; 
import type { Key } from '@/types/identifiers';
import type { Position } from 'cytoscape';

type InferenceJobDisplayProps = {
    /** The current inference job. */
    job: Job;
    /** The schema category used in the job. */
    schemaCategory: Category;
    /** List of inference edits. */
    inferenceEdits: InferenceEdit[];
    /** The current layout type. */
    layoutType: LayoutType;
    /** The candidates for merges and edits. */
    candidates: Candidates;
};

/**
 * Props passed to the component.
 */
const props = defineProps<InferenceJobDisplayProps>();

const graph = shallowRef<Graph>();

const showSignatures = ref(true); 

/**
 * Emits custom events to the parent component.
 */
const emit = defineEmits<{
    (e: 'change-layout', newLayoutType: LayoutType): void;
    (e: 'update-edit', edit: InferenceEdit): void;
    (e: 'cancel-edit'): void;
    (e: 'save-positions', map: Map<Key, Position>): void; 
}>();

/**
 * Watches for changes in the schema category and updates the graph when it changes.
 * If the graph is available, it assigns the graph to the new schema category.
 */
watch(() => props.schemaCategory, (newCategory, oldCategory) => {
    if (newCategory && newCategory !== oldCategory) {
        if (graph.value) {
            newCategory.graph = graph.value;

            graph.value.toggleEdgeLabels(showSignatures.value);
        }
    }
}, { immediate: true });


/**
 * Handles the graph creation event from the GraphDisplay component.
 */
function graphCreated(newGraph: Graph) {
    graph.value = newGraph;
    if (!props.schemaCategory)
        return;
    
    // eslint-disable-next-line vue/no-mutating-props
    props.schemaCategory.graph = newGraph;
}

/**
 * Emits the 'change-layout' event when the layout type is changed.
 */
function changeLayout(newLayoutType: LayoutType) {
    emit('change-layout', newLayoutType);
}


/**
 * Creates a reference merge edit from the provided payload.
 * Emits the 'update-edit' event with the new edit.
 */
function createReferenceMergeEdit(payload: Node[] | ReferenceCandidate) {
    let edit;

    if (payload instanceof ReferenceCandidate) {
        edit = new ReferenceMergeInferenceEdit(payload, true);
    }
    else {
        const referencingKey = payload[0].schemaObjex.key;
        const referencedKey = payload[1].schemaObjex.key;

        edit = new ReferenceMergeInferenceEdit(referencingKey, referencedKey, true);
    }
    confirmOrRevert(edit);
}

/**
 * Creates a primary key merge edit from the provided payload.
 * Emits the 'update-edit' event with the new edit.
 */
function createPrimaryKeyMergeEdit(payload: Node[] | PrimaryKeyCandidate) {
    let edit;

    if (payload instanceof PrimaryKeyCandidate) {
        edit = new PrimaryKeyMergeInferenceEdit(payload, true);
    }
    else {
        const primaryKey = payload[0].schemaObjex.key;
        const primaryKeyIdentified = payload[1].schemaObjex.key;

        edit = new PrimaryKeyMergeInferenceEdit(primaryKey, primaryKeyIdentified, true);
    }
    confirmOrRevert(edit);
}

/**
 * Creates a cluster edit from the selected nodes.
 * Emits the 'update-edit' event with the new cluster edit.
 */
function createClusterEdit(nodes: Node[]) {
    const clusterKeys = nodes.map(node => node.schemaObjex.key);

    const edit = new ClusterInferenceEdit(clusterKeys, true);
    confirmOrRevert(edit);
}

/**
 * Creates a recursion edit based on the provided nodes and edges.
 * Emits the 'update-edit' event with the new recursion edit.
 */
function createRecursionEdit(payload: { nodes: Node[], edges: Edge[] }) {
    const { nodes, edges } = payload;

    const pattern: PatternSegment[] = [];

    for (let i = 0; i < nodes.length; i++) {
        const node = nodes[i];
        const nodeName = node.metadata.label;
        let direction: '->' | '<-' | '' = '';

        if (i < edges.length) {
            const edge = edges[i];
            direction = edge.domainNode.equals(node) ? '->' : '<-';
        }

        pattern.push({ nodeName, direction });
    }

    // last segment direction should be empty
    if (pattern.length > 0) {
        const lastSegment = pattern[pattern.length - 1];
        pattern[pattern.length - 1] = new PatternSegment(lastSegment.nodeName, '');
    }
    

    const edit = new RecursionInferenceEdit(pattern, true);   
    confirmOrRevert(edit);
}

/**
 * Confirms or reverts the provided edit by emitting the 'update-edit' event.
 */
function confirmOrRevert(edit: InferenceEdit) {
    emit('update-edit', edit);
}

/**
 * Cancels the current edit by emitting the 'cancel-edit' event.
 */
function cancelEdit() {
    emit('cancel-edit');
}

/**
 * Handles signatures view when graph updated
 */
function handleShowSignaturesUpdate(newState: boolean) {
    showSignatures.value = newState;
    if (graph.value)
        graph.value.toggleEdgeLabels(newState);
}

const updatedPositionsMap = ref(new Map<Key, Position>());

function updatePosition(key: Key, newPosition: Position) {
    updatedPositionsMap.value.set(key, newPosition);
}

/**
 * Emits the 'save-positions' even when positions are saved.
 */
function savePositions() {
    if (updatedPositionsMap.value.size > 0) {
        emit('save-positions', updatedPositionsMap.value);
        updatedPositionsMap.value.clear();
    }
}
</script>

<template>
    <div
        v-if="job"
        class="d-flex flex-column"
    >
        <div class="divide">
            <GraphDisplay 
                @graph-created="graphCreated"
                @update-show-signatures="handleShowSignaturesUpdate"
            />
            <div
                v-if="graph"
                class="w-100 d-flex flex-column gap-2"
            >
                <EditorForInferenceSchemaCategory 
                    :graph="graph" 
                    :schema-category="props.schemaCategory" 
                    :inference-edits="props.inferenceEdits"
                    :candidates="props.candidates"
                    @confirm-reference-merge="createReferenceMergeEdit"    
                    @confirm-primary-key-merge="createPrimaryKeyMergeEdit"
                    @confirm-cluster="createClusterEdit"
                    @confirm-recursion="createRecursionEdit"
                    @cancel-edit="cancelEdit"   
                    @revert-edit="confirmOrRevert"    
                    @update-position="updatePosition"
                />

                <div class="flex-grow-1" />

                <LayoutSelector
                    :layout-type="props.layoutType"
                    @change-layout="changeLayout"
                />

                <div class="w-100 py-2 d-flex gap-2">
                    <slot name="button-row" />

                    <button
                        :disabled="!updatedPositionsMap.size"
                        @click="savePositions"
                    >
                        Save Positions
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
