<script setup lang="ts">
import { ErrorType, QueryDescription, QueryVersion } from '@/types/query';
import TextArea from '@/components/input/TextArea.vue';
import VersionDisplay from '../VersionDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { computed, ref, shallowRef } from 'vue';
import API from '@/utils/api';
import OpenCloseToggle from '@/components/common/OpenCloseToggle.vue';
import type { QueryResult } from '@/utils/api/routes/queries';
import QueryResultDisplay from './QueryResultDisplay.vue';
import QueryUpdateErrorDisplay from './QueryUpdateErrorDisplay.vue';
import QueryUpdateErrorBadge from './QueryUpdateErrorBadge.vue';
import QueryDescriptionDisplay from './QueryDescriptionDisplay.vue';
import QueryErrorDisplay from './QueryErrorDisplay.vue';

type QueryDisplayProps = {
    version: QueryVersion;
    defaultIsOpened?: boolean;
    defaultResult?: QueryResult;
    defaultError?: any;
};

const props = defineProps<QueryDisplayProps>();
const categoryInfo = useSchemaCategoryInfo();

const emit = defineEmits<{
    (e: 'createQueryVersion', version: QueryVersion): void;
    (e: 'deleteQuery'): void;
}>();

const content = ref(props.version.content);
const errors = ref(props.version.errors);
const uniqueErrors = computed(() => {
    const output = new Map<ErrorType, number>();
    for (const error of errors.value)
        output.set(error.type, (output.get(error.type) ?? 0) + 1);
    return [ ...output.entries() ].map(([ type, count ]) => ({ type, count }));
});

const isVersionOld = computed(() => props.version.version !== categoryInfo.value.versionId);

const isFetching = ref(false);

async function saveChanges() {
    isFetching.value = true;
    const result = await API.queries.updateQueryVersion({ versionId: props.version.id }, {
        version: categoryInfo.value.versionId,
        content: content.value,
        errors: errors.value,
    });
    isFetching.value = false;
    if (!result.status)
        return;

    const newVersion = QueryVersion.fromServer(result.data, props.version.query);
    emit('createQueryVersion', newVersion);
}

const isOpened = ref(props.defaultIsOpened);
const isChanged = computed(() => content.value !== props.version.content || errors.value.length !== props.version.errors.length);

const queryError = ref(props.defaultError);
const queryResult = ref(props.defaultResult);
const isExecuting = ref(false);

// TODO Extremely ugly code (also duplicated in NewQueryDisplay). Refactor.
// Probably create one variable for "queryState" or something.

async function executeQuery() {
    isExecuting.value = true;
    // queryResult.value = await API.queries.execute({}, { categoryId: categoryInfo.value.id, queryString: content.value });
    const result = await API.queries.execute({}, { categoryId: categoryInfo.value.id, queryString: content.value });
    isExecuting.value = false;
    if (result.status) {
        queryError.value = undefined;
        queryResult.value = result.data;
    }
    else {
        queryError.value = result.error;
    }
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

async function deleteQuery() {
    const result = await API.queries.deleteQuery({ queryId: props.version.query.id });
    if (result.status)
        emit('deleteQuery');
}
</script>

<template>
    <div class="p-3 border border-1 border-primary d-flex flex-column gap-3">
        <div class="d-flex align-items-center justify-content-between gap-3">
            <div class="d-flex align-items-baseline gap-3">
                <h3 class="m-0">
                    {{ props.version.query.label }}{{ isChanged ? '*' : '' }}
                </h3>
                <span>
                    v.
                    <VersionDisplay
                        :version-id="version.version"
                        :error="isVersionOld"
                    />
                </span>
            </div>
            <div class="d-flex align-items-center gap-3">
                <template v-if="isOpened">
                    <button
                        v-if="isChanged"
                        :disabled="isExecuting || isFetching"
                        class="primary"
                        @click="saveChanges"
                    >
                        Save
                    </button>
                    <button
                        :disabled="isExecuting"
                        @click="deleteQuery"
                    >
                        Delete
                    </button>
                    <button
                        :disabled="isExecuting"
                        @click="executeQuery"
                    >
                        Execute
                    </button>
                    <button
                        :disabled="isExecuting"
                        @click="describeQuery"
                    >
                        Describe
                    </button>
                </template>
                <template v-else>
                    <QueryUpdateErrorBadge
                        v-for="error in uniqueErrors"
                        :key="error.type"
                        :type="error.type"
                        :count="error.count"
                    />
                </template>
                <OpenCloseToggle v-model="isOpened" />
            </div>
        </div>
        <template v-if="isOpened">
            <QueryUpdateErrorDisplay
                v-for="(error, index) in errors"
                :key="index"
                :error="error"
                @delete-error="errors = errors.filter((_, i) => i !== index)"
            />
            <TextArea
                v-model="content"
                class="w-100"
            />
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
