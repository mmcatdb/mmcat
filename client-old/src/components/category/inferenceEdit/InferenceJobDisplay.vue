<script setup lang="ts">
import { computed, onMounted, ref, shallowRef, watch } from 'vue';
import API from '@/utils/api';
import { Job } from '@/types/job';
import type { Graph, Node } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import { SchemaCategory } from '@/types/schema';
import { isInferenceJobData } from '@/utils/InferenceJobData';
import EditorForInferenceSchemaCategory from '@/components/category/inferenceEdit/EditorForInferenceSchemaCategory.vue'
import type { AbstractInferenceEdit } from '@/types/inferenceEdit/inferenceEdit'
import { ReferenceMergeInferenceEdit } from '@/types/inferenceEdit/inferenceEdit'; 

type InferenceJobDisplayProps = {
    job: Job;
    schemaCategory: SchemaCategory;
};

const props = defineProps<InferenceJobDisplayProps>();

const graph = shallowRef<Graph>();

const emit = defineEmits<{
    (e: 'updateEdit', edit: AbstractInferenceEdit): void;
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

function createMergeEdit(nodes: (Node)[]) {
    const referenceKey = nodes[0].schemaObject.key;
    const referredKey = nodes[1].schemaObject.key;

    const edit = new ReferenceMergeInferenceEdit(referenceKey, referredKey);
    confirm(edit);
}

function confirm(edit: AbstractInferenceEdit) {
    emit('updateEdit', edit);
}

function cancelEdit() {
    emit('cancel-edit');
}

</script>
<!--
<template>
    <div
        v-if="job"
        class="d-flex flex-column"
    >
            <div class="divide">
            <GraphDisplay 
                @graph-created="graphCreated"
            />
            <div v-if="graph">
                <EditorForInferenceSchemaCategory 
                    :graph="graph" 
                    :schema-category="props.schemaCategory" 
                    @merge-confirm="createMergeEdit"    
                    @cancel-edit="cancelEdit"            
                />
            </div>
        </div> 
    </div>
</template> -->

<template>
    <div v-if="job" class="d-flex flex-column">
        <div class="divide">
            <GraphDisplay @graph-created="graphCreated" />
            <div v-if="graph">
                <EditorForInferenceSchemaCategory 
                    :graph="graph" 
                    :schema-category="props.schemaCategory" 
                    @merge-confirm="createMergeEdit"    
                    @cancel-edit="cancelEdit"            
                />
                <slot name="below-editor"></slot>
            </div>
        </div>
    </div>
</template>