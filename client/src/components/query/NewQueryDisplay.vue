<script setup lang="ts">
import TextArea from '@/components/input/TextArea.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { computed, ref } from 'vue';
import API from '@/utils/api';
import SaveQueryButton from '@/components/query/SaveQueryButton.vue';
import QueryResultDisplay from './QueryResultDisplay.vue';
import type { Result } from '@/types/api/result';
import type { QueryResult } from '@/utils/api/routes/queries';
import OpenCloseToggle from '../common/OpenCloseToggle.vue';
import type { QueryWithVersion } from '@/types/query';
import QueryDisplay from './QueryDisplay.vue';

const DEFAULT_QUERY_STRING = `SELECT {
    ?product
        productId ?id ;
        name ?label ;
        totalPrice ?price .
}
WHERE {
    ?product
        15 ?id ;
        16 ?label ;
        17 ?price .
}`;

export type QueryLog = {
    id: number;
    content: string;
    result: Result<QueryResult>;
};

type QueryDisplayProps = {
    initialData?: QueryLog;
};

const props = defineProps<QueryDisplayProps>();

const emit = defineEmits<{
    (e: 'executeQuery', content: string, result: Result<QueryResult>): void;
}>();

const categoryInfo = useSchemaCategoryInfo();

const content = ref(props.initialData?.content ?? DEFAULT_QUERY_STRING);

const queryResult = ref(props.initialData?.result);
const isError = computed(() => queryResult.value?.status === false);
const isExecuting = ref(false);

async function execute() {
    const contentValue = content.value;
    isExecuting.value = true;
    queryResult.value = await API.queries.execute({}, { categoryId: categoryInfo.value.id, queryString: contentValue });
    isExecuting.value = false;
    emit('executeQuery', contentValue, queryResult.value);
}

const isOpened = ref(!props.initialData);

const savedQuery = ref<QueryWithVersion>();

function querySaved(query: QueryWithVersion) {
    savedQuery.value = query;
}
</script>

<template>
    <template v-if="savedQuery">
        <QueryDisplay
            :version="savedQuery.version"
            default-is-opened
            :default-result="queryResult"
        />
    </template>
    <template v-else>
        <div class="p-3 border border-1 border-secondary d-flex flex-column gap-3">
            <template v-if="initialData">
                <div class="d-flex align-items-center justify-content-between gap-3">
                    <h3
                        class="m-0"
                        :class="{ 'text-danger': isError}"
                    >
                        #{{ initialData.id }}
                    </h3>
                    <div class="d-flex align-items-center gap-3">
                        <template v-if="isOpened">
                            <button
                                :disabled="isExecuting"
                                @click="execute"
                            >
                                {{ initialData && queryResult ? 'Re-execute' : 'Execute' }}
                            </button>
                            <SaveQueryButton
                                :content="content"
                                @save-query="querySaved"
                            />
                        </template>
                        <OpenCloseToggle v-model="isOpened" />
                    </div>
                </div>
            </template>
            <template v-if="isOpened">
                <TextArea
                    v-model="content"
                    class="w-100"
                />
                <template v-if="!initialData">
                    <div class="d-flex align-items-center gap-3">
                        <button
                            :disabled="isExecuting"
                            @click="execute"
                        >
                            {{ initialData && queryResult ? 'Re-execute' : 'Execute' }}
                        </button>
                    </div>
                </template>
                <QueryResultDisplay
                    :result="queryResult"
                    :is-executing="isExecuting"
                />
            </template>
        </div>
    </template>
</template>
