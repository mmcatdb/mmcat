<script setup lang="ts">
import { shallowRef, watch } from 'vue';
import type { Job } from '@/types/job';
import type { Graph, Node, Edge } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import type { SchemaCategory } from '@/types/schema';
import EditorForInferenceSchemaCategory from '@/components/category/inference/EditorForInferenceSchemaCategory.vue';
import { type InferenceEdit, RecursionInferenceEdit, ClusterInferenceEdit, PrimaryKeyMergeInferenceEdit, ReferenceMergeInferenceEdit, PatternSegment } from '@/types/inference/inferenceEdit'; 
import { Candidates } from '@/types/inference/candidates'; 

type InferenceJobDisplayProps = {
    job: Job;
    schemaCategory: SchemaCategory;
    inferenceEdits: InferenceEdit[];
    candidates: Candidates;
};

const props = defineProps<InferenceJobDisplayProps>();

const graph = shallowRef<Graph>();

const emit = defineEmits<{
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
    if (!props.schemaCategory) {
        console.log('This should not happen. - schemaCategory.value empty');
        return;
    }
    // eslint-disable-next-line vue/no-mutating-props
    props.schemaCategory.graph = newGraph;
}

function createReferenceMergeEdit(nodes: Node[]) {
    const referenceKey = nodes[0].schemaObject.key;
    const referredKey = nodes[1].schemaObject.key;

    const edit = new ReferenceMergeInferenceEdit(referenceKey, referredKey, true);
    confirmOrRevert(edit);
}

function createPrimaryKeyMergeEdit(nodes: Node[]) {
    const primaryKey = nodes[0].schemaObject.key;

    const edit = new PrimaryKeyMergeInferenceEdit(primaryKey, true);
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
