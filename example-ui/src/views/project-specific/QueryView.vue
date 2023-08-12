<script setup lang="ts">
import TextArea from '@/components/input/TextArea.vue';
import queries from '@/utils/api/routes/queries';
import { useSchemaCategoryId } from '@/utils/injects';
import { computed, ref } from 'vue';

const queryString = ref(`SELECT {
    ?customer has ?id .
}
WHERE {
    ?customer 1 ?id .
}`);

type QueryLog = {
    query: string;
    result: string;
    id: number;
};

const logs = ref<QueryLog[]>([]);
const queryResult = ref<string>();

const categoryId = useSchemaCategoryId();

let lastQueryId = 0;

async function executeQuery() {
    const query = queryString.value;
    queryResult.value = undefined;

    const response = await queries.execute({}, { categoryId, queryString: query });
    const result = response.status
        ? response.data.jsonValues.join(',\n')
        : 'Error :(';

    queryResult.value = result;
    logs.value.push({ query, result, id: lastQueryId++ });
}

const displayedLogs = computed(() => logs.value.length > 1 ? logs.value.slice(0, logs.value.length - 1).reverse() : undefined);
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
        <template v-if="displayedLogs">
            <h2 class="mt-4">
                Previous queries
            </h2>
            <div
                v-for="log in displayedLogs"
                :key="log.id"
                class="mt-4 d-flex"
            >
                <TextArea
                    v-model="log.query"
                    class="query-input"
                    :min-rows="1"
                />
                <TextArea
                    v-model="log.result"
                    class="query-output ms-4"
                    :min-rows="1"
                />
            </div>
        </template>
    </div>
</template>

<style scoped>
.query-input, .query-output {
    min-width: 600px;
}
</style>
