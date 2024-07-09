<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue';
import API from '@/utils/api';
import { Job, JobState } from '@/types/job';
import { ActionType } from '@/types/action';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import type { Graph, Node } from '@/types/categoryGraph';
import GraphDisplay from '../../category/GraphDisplay.vue';
import { SchemaCategory } from '@/types/schema';
import { isInferenceJobData } from '@/utils/InferenceJobData';
import EditorForInferenceSchemaCategory from '@/components/category/inferenceEdit/EditorForInferenceSchemaCategory.vue'
import { useSchemaCategoryId } from '@/utils/injects';

type InferenceJobDisplayProps = {
    job: Job;
};

/*	InferenceJobDisplay - vyrobí graf, předá jej do SK (kterou získá z jobu),
 bude mít GraphDisplay a EditorForInferenceSchemaCategory.
*/

const props = defineProps<InferenceJobDisplayProps>();
const graph = shallowRef<Graph>();

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


// TODO this is just example
/*
const SK = shallowRef<SchemaCategory>();

function graphCreated(newGraph: Graph) {
    graph.value = newGraph;
    if (!SK.value) {
        console.log("This should not happen.")
        return;
    }
    SK.value.graph = newGraph;
}

const categoryId = useSchemaCategoryId()

onMounted(async () => {
    const schemaCategoryResult = await API.schemas.getCategoryWrapper({ id: categoryId });
    if (!schemaCategoryResult.status) {
        // TODO handle error
        return;
    }

    SK.value = SchemaCategory.fromServer(schemaCategoryResult.data, []);
});*/

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
                <EditorForInferenceSchemaCategory :graph="graph" :schema-category="schemaCategory" />
            </div>
        </div> 
    </div>
</template>