<script setup lang="ts">
import { QueryVersion } from '@/types/query';
import TextArea from '@/components/input/TextArea.vue';
import VersionDisplay from '../VersionDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { computed, ref } from 'vue';
import API from '@/utils/api';
import OpenCloseToggle from '@/components/common/OpenCloseToggle.vue';
import type { QueryResult } from '@/utils/api/routes/queries';
import type { Result } from '@/types/api/result';
import QueryResultDisplay from './QueryResultDisplay.vue';

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

enum State { Display, Editing, Fetching }

const content = ref(props.version.content);
const errors = ref(props.version.errors);
const state = ref(State.Display);

const isVersionOld = computed(() => props.version.version !== categoryInfo.value.versionId);

function cancelEditing() {
    state.value = State.Display;
    content.value = props.version.content;
}

async function saveChanges() {
    state.value = State.Fetching;
    const result = await API.queries.updateQueryVersion({ versionId: props.version.id }, {
        version: categoryInfo.value.versionId,
        content: content.value,
        errors: errors.value,
    });
    state.value = State.Display;
    if (!result.status)
        return;

    const newVersion = QueryVersion.fromServer(result.data, props.version.query);
    emit('createQueryVersion', newVersion);
}

const isOpened = ref(props.defaultIsOpened);

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
                    {{ props.version.query.label }}
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
                    <template v-if="isVersionOld">
                        <template v-if="state === State.Display">
                            <button
                                :disabled="isExecuting"
                                @click="() => state = State.Editing"
                            >
                                Update
                            </button>
                        </template>
                        <template v-else>
                            <button
                                :disabled="state === State.Fetching || isExecuting"
                                @click="saveChanges"
                            >
                                Save
                            </button>
                            <button
                                :disabled="state === State.Fetching || isExecuting"
                                @click="cancelEditing"
                            >
                                Cancel
                            </button>
                        </template>
                    </template>
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
                <OpenCloseToggle v-model="isOpened" />
            </div>
        </div>
        <template v-if="isOpened">
            <TextArea
                v-model="content"
                class="w-100"
                :disabled="state !== State.Editing"
            />
            <QueryResultDisplay
                :result="queryResult"
                :is-executing="isExecuting"
            />
        </template>
    </div>
</template>
