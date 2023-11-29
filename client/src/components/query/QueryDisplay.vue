<script setup lang="ts">
import { ErrorType, QueryVersion } from '@/types/query';
import TextArea from '@/components/input/TextArea.vue';
import VersionDisplay from '../VersionDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { computed, ref } from 'vue';
import API from '@/utils/api';
import OpenCloseToggle from '@/components/common/OpenCloseToggle.vue';
import type { QueryResult } from '@/utils/api/routes/queries';
import type { Result } from '@/types/api/result';
import QueryResultDisplay from './QueryResultDisplay.vue';
import QueryUpdateErrorDisplay from './QueryUpdateErrorDisplay.vue';
import QueryUpdateErrorBadge from './QueryUpdateErrorBadge.vue';

type QueryDisplayProps = {
    version: QueryVersion;
    defaultIsOpened?: boolean;
    defaultResult?: Result<QueryResult>;
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

const queryResult = ref(props.defaultResult);
const isExecuting = ref(false);

async function executeQuery() {
    isExecuting.value = true;
    queryResult.value = await API.queries.execute({}, { categoryId: categoryInfo.value.id, queryString: content.value });
    isExecuting.value = false;
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
            <QueryResultDisplay
                :result="queryResult"
                :is-executing="isExecuting"
            />
        </template>
    </div>
</template>
