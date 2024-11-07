<script setup lang="ts">
import TextArea from '@/components/input/TextArea.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { ref, shallowRef } from 'vue';
import API from '@/utils/api';
import SaveQueryButton from '@/components/query/SaveQueryButton.vue';
import QueryResultDisplay from './QueryResultDisplay.vue';
import type { Result } from '@/types/api/result';
import type { QueryResult } from '@/utils/api/routes/queries';
import OpenCloseToggle from '../common/OpenCloseToggle.vue';
import { Query, QueryDescription } from '@/types/query';
import QueryDisplay from './QueryDisplay.vue';
import QueryDescriptionDisplay from './QueryDescriptionDisplay.vue';
import QueryErrorDisplay from './QueryErrorDisplay.vue';

/* Example:
SELECT {
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
}
*/

const DEFAULT_QUERY_STRING = '';

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

const queryError = ref(props.initialData?.result?.status === false ? props.initialData?.result.error : undefined);
const queryResult = ref(props.initialData?.result?.status ? props.initialData?.result.data : undefined);
const isExecuting = ref(false);

async function executeQuery() {
    const contentValue = content.value;
    isExecuting.value = true;
    const result = await API.queries.execute({}, { categoryId: categoryInfo.value.id, queryString: contentValue });
    isExecuting.value = false;
    if (result.status) {
        queryError.value = undefined;
        queryResult.value = result.data;
    }
    else {
        queryError.value = result.error;
    }
    emit('executeQuery', contentValue, result);
}

const isOpened = ref(!props.initialData);

const savedQuery = ref<Query>();

function querySaved(query: Query) {
    savedQuery.value = query;
}

const queryDescription = shallowRef<QueryDescription>();

async function describeQuery() {
    isExecuting.value = true;
    const result = await API.queries.describe({}, { categoryId: categoryInfo.value.id, queryString: content.value });
    isExecuting.value = false;
    if (result.status) {
        queryError.value = undefined;
        queryDescription.value = QueryDescription.fromServer(result.data);
    }
    else {
        queryError.value = result.error;
    }
}
</script>

<template>
    <template v-if="savedQuery">
        <QueryDisplay
            :query="savedQuery"
            default-is-opened
            :default-result="queryResult"
            :default-error="queryError"
        />
    </template>
    <template v-else>
        <div class="p-3 border border-1 border-secondary d-flex flex-column gap-3">
            <template v-if="initialData">
                <div class="d-flex align-items-center justify-content-between gap-3">
                    <h3
                        class="m-0"
                        :class="{ 'text-danger': !!queryError}"
                    >
                        #{{ initialData.id }}
                    </h3>
                    <div class="d-flex align-items-center gap-3">
                        <template v-if="isOpened">
                            <button
                                :disabled="isExecuting"
                                @click="executeQuery"
                            >
                                {{ initialData && queryResult ? 'Re-execute' : 'Execute' }}
                            </button>
                            <button
                                :disabled="isExecuting"
                                @click="describeQuery"
                            >
                                Describe
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
                            @click="executeQuery"
                        >
                            {{ initialData && queryResult ? 'Re-execute' : 'Execute' }}
                        </button>
                        <button
                            :disabled="isExecuting"
                            @click="describeQuery"
                        >
                            Describe
                        </button>
                    </div>
                </template>
                <QueryErrorDisplay
                    v-if="queryError"
                    :error="queryError"
                    :is-executing="isExecuting"
                />
                <template v-else>
                    <QueryResultDisplay
                        v-if="queryResult"
                        :result="queryResult"
                        :is-executing="isExecuting"
                    />
                    <QueryDescriptionDisplay
                        v-if="queryDescription"
                        :description="queryDescription"
                        :is-executing="isExecuting"
                    />
                </template>
            </template>
        </div>
    </template>
</template>
