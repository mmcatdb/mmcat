<script setup lang="ts">
import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import { Job } from '@/types/job';
import type { Graph, Node } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import { SchemaCategory } from '@/types/schema';
import EditorForInferenceSchemaCategory from '@/components/category/inferenceEdit/EditorForInferenceSchemaCategory.vue'
import type { AbstractInferenceEdit } from '@/types/inferenceEdit/inferenceEdit'
import { RecursionInferenceEdit, ClusterInferenceEdit, PrimaryKeyMergeInferenceEdit, ReferenceMergeInferenceEdit } from '@/types/inferenceEdit/inferenceEdit'; 

type InferenceJobDisplayProps = {
    job: Job;
    schemaCategory: SchemaCategory;
};

const props = defineProps<InferenceJobDisplayProps>();

const graph = shallowRef<Graph>();

const emit = defineEmits<{
    (e: 'update-edit', edit: AbstractInferenceEdit): void;
    (e: 'cancel-edit'): void;
}>();

watch(() => props.schemaCategory, (newCategory, oldCategory) => {
    if (newCategory && newCategory !== oldCategory) {
        if (graph.value) {
            newCategory.graph = graph.value;
        }
    }
}, { immediate: true });

function graphCreated(newGraph: Graph) {
    graph.value = newGraph;
    if (!props.schemaCategory) {
        console.log("This should not happen. - schemaCategory.value empty")
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
    const primaryKeyRoot = nodes[0].schemaObject.key;
    const primaryKey = nodes[1].schemaObject.key;

    const edit = new PrimaryKeyMergeInferenceEdit(primaryKeyRoot, primaryKey);
    confirm(edit);
}

function createClusterEdit(nodes: (Node)[]) {
    const clusterKeys = nodes.map(node => node.schemaObject.key);

    const edit = new ClusterInferenceEdit(clusterKeys);
    confirm(edit);
}

function createRecursionEdit(nodes: (Node)[], edges: (Edge)[]) {

    const edit = new RecursionInferenceEdit();
    confirm(edit);
}

function confirm(edit: AbstractInferenceEdit) {
    emit('update-edit', edit);
}

function cancelEdit() {
    emit('cancel-edit');
}

</script>

<template>
    <div v-if="job" class="d-flex flex-column">
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
                <slot name="below-editor"></slot>
            </div>
        </div>
    </div>
</template>