<script setup lang="ts">
import { shallowRef, watch } from 'vue';
import type { Job } from '@/types/job';
import type { Graph, Node, Edge } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import type { SchemaCategory } from '@/types/schema';
import EditorForInferenceSchemaCategory from './EditorForInferenceSchemaCategory.vue';
import LayoutSelector from './LayoutSelector.vue';
import type { LayoutType } from '@/types/inference/layoutType';
import { type InferenceEdit, RecursionInferenceEdit, ClusterInferenceEdit, PrimaryKeyMergeInferenceEdit, ReferenceMergeInferenceEdit, PatternSegment } from '@/types/inference/inferenceEdit'; 
import { Candidates, ReferenceCandidate, PrimaryKeyCandidate } from '@/types/inference/candidates'; 

type InferenceJobDisplayProps = {
    job: Job;
    schemaCategory: SchemaCategory;
    inferenceEdits: InferenceEdit[];
    layoutType: LayoutType;
    candidates: Candidates;
};

/**
 * Props passed to the component.
 * @typedef {Object} InferenceJobDisplayProps
 * @property {Job} job - The current inference job.
 * @property {SchemaCategory} schemaCategory - The schema category used in the job.
 * @property {InferenceEdit[]} inferenceEdits - List of inference edits.
 * @property {LayoutType} layoutType - The current layout type.
 * @property {Candidates} candidates - The candidates for merges and edits.
 */
const props = defineProps<InferenceJobDisplayProps>();

const graph = shallowRef<Graph>();

/**
 * Emits custom events to the parent component.
 * @emits change-layout - Emitted when the layout type is changed.
 * @emits update-edit - Emitted when an edit is updated.
 * @emits cancel-edit - Emitted when the current edit is canceled.
 * @param {LayoutType} newLayoutType - The new layout type.
 * @param {InferenceEdit} edit - The updated inference edit.
 */
const emit = defineEmits<{
    (e: 'change-layout', newLayoutType: LayoutType): void;
    (e: 'update-edit', edit: InferenceEdit): void;
    (e: 'cancel-edit'): void;
}>();

/**
 * Watches for changes in the schema category and updates the graph when it changes.
 * If the graph is available, it assigns the graph to the new schema category.
 */
watch(() => props.schemaCategory, (newCategory, oldCategory) => {
    if (newCategory && newCategory !== oldCategory) {
        if (graph.value) 
            newCategory.graph = graph.value;
        
    }
}, { immediate: true });

/**
 * Handles the graph creation event from the GraphDisplay component.
 * @param {Graph} newGraph - The newly created graph instance.
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
 * @param {LayoutType} newLayoutType - The new layout type.
 */
function changeLayout(newLayoutType: LayoutType) {
    emit('change-layout', newLayoutType);
}


/**
 * Creates a reference merge edit from the provided payload.
 * Emits the 'update-edit' event with the new edit.
 * @param {Node[] | ReferenceCandidate} payload - The nodes or reference candidate for the merge.
 */
function createReferenceMergeEdit(payload: Node[] | ReferenceCandidate) {
    let edit;

    if (payload instanceof ReferenceCandidate) {
        edit = new ReferenceMergeInferenceEdit(payload, true);
    }
    else {
        const referenceKey = payload[0].schemaObject.key;
        const referredKey = payload[1].schemaObject.key;

        edit = new ReferenceMergeInferenceEdit(referenceKey, referredKey, true);
    }
    confirmOrRevert(edit);
}

/**
 * Creates a primary key merge edit from the provided payload.
 * Emits the 'update-edit' event with the new edit.
 * @param {Node[] | PrimaryKeyCandidate} payload - The nodes or primary key candidate for the merge.
 */
function createPrimaryKeyMergeEdit(payload: Node[] | PrimaryKeyCandidate) {
    let edit;

    if (payload instanceof PrimaryKeyCandidate) {
        edit = new PrimaryKeyMergeInferenceEdit(payload, true);
    }
    else {
        const primaryKey = payload[0].schemaObject.key;
        const primaryKeyIdentified = payload[1].schemaObject.key;

        edit = new PrimaryKeyMergeInferenceEdit(primaryKey, primaryKeyIdentified, true);
    }
    confirmOrRevert(edit);
}

/**
 * Creates a cluster edit from the selected nodes.
 * Emits the 'update-edit' event with the new cluster edit.
 * @param {Node[]} nodes - The nodes to include in the cluster.
 */
function createClusterEdit(nodes: Node[]) {
    const clusterKeys = nodes.map(node => node.schemaObject.key);

    const edit = new ClusterInferenceEdit(clusterKeys, true);
    confirmOrRevert(edit);
}

/**
 * Creates a recursion edit based on the provided nodes and edges.
 * Emits the 'update-edit' event with the new recursion edit.
 * @param {{nodes: Node[], edges: Edge[]}} payload - The nodes and edges involved in the recursion.
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
 * @param {InferenceEdit} edit - The edit to confirm or revert.
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

</script>

<template>
    <div
        v-if="job"
        class="d-flex flex-column"
    >
        <div class="divide">
            <GraphDisplay @graph-created="graphCreated" />
            <div v-if="graph">
                <LayoutSelector
                    :layout-type="props.layoutType"
                    @change-layout="changeLayout"
                />
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
                />
                <slot name="below-editor" />
            </div>
        </div>
    </div>
</template>