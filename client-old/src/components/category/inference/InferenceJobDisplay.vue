<script setup lang="ts">
import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import { Job } from '@/types/job';
import type { Graph, Node, Edge } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import { SchemaCategory } from '@/types/schema';
import EditorForInferenceSchemaCategory from '@/components/category/inference/EditorForInferenceSchemaCategory.vue';
import { type InferenceEdit, RecursionInferenceEdit, ClusterInferenceEdit, PrimaryKeyMergeInferenceEdit, ReferenceMergeInferenceEdit, PatternSegment } from '@/types/inference/inferenceEdit'; 

type InferenceJobDisplayProps = {
    job: Job;
    schemaCategory: SchemaCategory;
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
    props.schemaCategory.graph = newGraph;
}

function createReferenceMergeEdit(nodes: (Node)[]) {
    const referenceKey = nodes[0].schemaObject.key;
    const referredKey = nodes[1].schemaObject.key;

    const edit = new ReferenceMergeInferenceEdit(referenceKey, referredKey);
    confirm(edit);
}

function createPrimaryKeyMergeEdit(nodes: (Node)[]) {
    const primaryKey = nodes[0].schemaObject.key;

    const edit = new PrimaryKeyMergeInferenceEdit(primaryKey);
    confirm(edit);
}

function createClusterEdit(nodes: (Node)[]) {
    const clusterKeys = nodes.map(node => node.schemaObject.key);

    const edit = new ClusterInferenceEdit(clusterKeys);
    confirm(edit);
}

function createRecursionEdit(payload: { nodes: (Node)[], edges: (Edge)[] }) {
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
    if (pattern.length > 0) 
        pattern[pattern.length - 1].direction = '';
    

    const edit = new RecursionInferenceEdit(pattern);   
    confirm(edit);
}

function confirm(edit: InferenceEdit) {
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
                    @confirm-reference-merge="createReferenceMergeEdit"    
                    @confirm-primary-key-merge="createPrimaryKeyMergeEdit"
                    @confirm-cluster="createClusterEdit"
                    @confirm-recursion="createRecursionEdit"
                    @cancel-edit="cancelEdit"            
                />
                <slot name="below-editor" />
            </div>
        </div>
    </div>
</template>@/types/inference/inferenceEdit@/types/inference/inferenceEdit