<script setup lang="ts">
import TextArea from '@/components/input/TextArea.vue';
import queries from '@/utils/api/routes/queries';
import { useSchemaCategoryId } from '@/utils/injects';
import { ref } from 'vue';

const queryString = ref(`SELECT {
    ?customer has ?id .
}
WHERE {
    ?customer 1 ?id .
}`);

const queryResult = ref<string>();

const categoryId = useSchemaCategoryId();

async function executeQuery() {
    const result = await queries.execute({}, {
        categoryId,
        queryString: queryString.value,
    });
    console.log(result);
    if (result.status)
        queryResult.value = result.data.jsonValues.join(',\n');
    else
        queryResult.value = undefined;
}
</script>

<template>
    <div>
        <h1>Query</h1>
        <div class="mt-4 d-flex">
            <TextArea
                v-model="queryString"
                class="query-input"
            />
            <TextArea
                v-if="queryResult !== undefined"
                v-model="queryResult"
                class="query-output ms-4"
            />
        </div>
        <button
            class="mt-4"
            @click="executeQuery"
        >
            Execute
        </button>
    </div>
</template>

<style scoped>
.query-input, .query-output {
    min-width: 600px;
}
</style>
