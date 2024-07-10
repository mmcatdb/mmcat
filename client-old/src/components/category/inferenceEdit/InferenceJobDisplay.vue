<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue';
import API from '@/utils/api';
import { Job } from '@/types/job';
import type { Graph, Node } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import { SchemaCategory } from '@/types/schema';
import { isInferenceJobData } from '@/utils/InferenceJobData';
import EditorForInferenceSchemaCategory from '@/components/category/inferenceEdit/EditorForInferenceSchemaCategory.vue'
import { useSchemaCategoryId } from '@/utils/injects';
import type { AbstractInferenceEdit } from '@/types/inferenceEdit/inferenceEdit'
import { ReferenceMergeInferenceEdit } from '@/types/inferenceEdit/inferenceEdit'; 

type InferenceJobDisplayProps = {
    job: Job;
};

/*	InferenceJobDisplay - vyrobí graf, předá jej do SK (kterou získá z jobu),
 bude mít GraphDisplay a EditorForInferenceSchemaCategory.
 A navic: bude mít na starost udržování operací - potvrzení/smazání, zobrazení toho seznamu, fečování na BE, ... 

*/

const props = defineProps<InferenceJobDisplayProps>();
const graph = shallowRef<Graph>();

const emit = defineEmits<{
    (e: 'updateEdit', edit: AbstractInferenceEdit): void;
}>();

const fetching = ref(false);

const schemaCategory = computed(() => {
    if (typeof props.job.result === 'string') {
        const parsedResult = JSON.parse(props.job.result);
        if (isInferenceJobData(parsedResult)) {
            return SchemaCategory.fromServer(parsedResult.inference.schemaCategory, []);
        } else {
            throw new Error("InferenceJobData is not the right type");            
        }
    }
    throw new Error("InferenceJobData is not the right type");
});

function graphCreated(newGraph: Graph) {
    graph.value = newGraph;
    if (!schemaCategory.value) {
        console.log("This should not happen. - schemaCategory.value empty")
        return;
    }
    schemaCategory.value.graph = newGraph;
}

function createMergeEdit(nodes: (Node)[]) {
    const referenceKey = nodes[0].schemaObject.key;
    const referredKey = nodes[1].schemaObject.key;

    const edit = new ReferenceMergeInferenceEdit(referenceKey, referredKey);
    console.log("edit referenceKey" + edit.referenceKey);
    confirm(edit);
}

function confirm(edit: AbstractInferenceEdit) {
    console.log("confirm in inferencejobdisplay");
    emit('updateEdit', edit);
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
            />
            <div v-if="graph">
                <EditorForInferenceSchemaCategory 
                    :graph="graph" 
                    :schema-category="schemaCategory" 
                    @merge-confirm="createMergeEdit"                
                />
            </div>
        </div> 
    </div>
</template>