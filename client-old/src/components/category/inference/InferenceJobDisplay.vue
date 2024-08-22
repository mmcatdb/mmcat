<script setup lang="ts">
import { shallowRef, watch } from 'vue';
import type { Job } from '@/types/job';
import type { Graph, Node, Edge } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import type { SchemaCategory } from '@/types/schema';
import EditorForInferenceSchemaCategory from './EditorForInferenceSchemaCategory.vue';
import LayoutSelector from './LayoutSelector.vue';
import { LayoutType } from '@/types/inference/layoutType';
import { type InferenceEdit, RecursionInferenceEdit, ClusterInferenceEdit, PrimaryKeyMergeInferenceEdit, ReferenceMergeInferenceEdit, PatternSegment } from '@/types/inference/inferenceEdit'; 
import { Candidates, ReferenceCandidate, PrimaryKeyCandidate } from '@/types/inference/candidates'; 

type InferenceJobDisplayProps = {
    job: Job;
    schemaCategory: SchemaCategory;
    inferenceEdits: InferenceEdit[];
    layoutType: LayoutType;
    candidates: Candidates;
};

const props = defineProps<InferenceJobDisplayProps>();

const graph = shallowRef<Graph>();

const emit = defineEmits<{
    (e: 'change-layout', newLayoutType: LayoutType): void;
    (e: 'update-edit', edit: InferenceEdit): void;
    (e: 'cancel-edit'): void;
}>();

watch(() => props.schemaCategory, (newCategory, oldCategory) => {
    if (newCategory && newCategory !== oldCategory) {
        if (graph.value) 
            newCategory.graph = graph.value;
        
    }
}, { immediate: true });

function graphCreated(newGraph: Graph) {
    graph.value = newGraph;
    if (!props.schemaCategory)
        return;
    
    // eslint-disable-next-line vue/no-mutating-props
    props.schemaCategory.graph = newGraph;
}

function changeLayout(newLayoutType: LayoutType) {
    emit('change-layout', newLayoutType);
}

function createReferenceMergeEdit(payload: Node[] | ReferenceCandidate) {
    let edit;

    if (payload instanceof ReferenceCandidate) {
        edit = new ReferenceMergeInferenceEdit(payload, true);
    } else {
        const referenceKey = payload[0].schemaObject.key;
        const referredKey = payload[1].schemaObject.key;

        edit = new ReferenceMergeInferenceEdit(referenceKey, referredKey, true);
    }
    confirmOrRevert(edit);
}

function createPrimaryKeyMergeEdit(payload: Node[] | PrimaryKeyCandidate) {
    let edit;

    if (payload instanceof PrimaryKeyCandidate) {
        edit = new PrimaryKeyMergeInferenceEdit(payload, true);
    } else {
        const primaryKey = payload[0].schemaObject.key;

        edit = new PrimaryKeyMergeInferenceEdit(primaryKey, true);
    }
    confirmOrRevert(edit);
}

function createClusterEdit(nodes: Node[]) {
    const clusterKeys = nodes.map(node => node.schemaObject.key);

    const edit = new ClusterInferenceEdit(clusterKeys, true);
    confirmOrRevert(edit);
}

function createRecursionEdit(payload: { nodes: Node[], edges: Edge[] }) {
    const { nodes, edges } = payload;

    const pattern: PatternSegment[] = [];

    for (let i = 0; i < nodes.length; i++) {
        const node = nodes[i];
        const nodeName = node.schemaObject.label;
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

function confirmOrRevert(edit: InferenceEdit) {
    emit('update-edit', edit);
}

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
