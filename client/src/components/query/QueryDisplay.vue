<script setup lang="ts">
import { QueryVersion } from '@/types/query';
import TextArea from '@/components/input/TextArea.vue';
import VersionDisplay from '../VersionDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { computed, ref } from 'vue';
import API from '@/utils/api';

type QueryDisplayProps = {
    version: QueryVersion;
};

const props = defineProps<QueryDisplayProps>();
const categoryInfo = useSchemaCategoryInfo();

const emit = defineEmits<{
    (e: 'createQueryVersion', version: QueryVersion): void;
}>();

enum State { Display, Editing, Fetching }

const content = ref(props.version.content);
const state = ref(State.Display);

const isVersionOld = computed(() => props.version.version !== categoryInfo.value.versionId);

function cancelEditing() {
    state.value = State.Display;
    content.value = props.version.content;
}

async function saveChanges() {
    state.value = State.Fetching;
    const result = await API.queries.createQueryVersion({ queryId: props.version.query.id }, {
        version: categoryInfo.value.versionId,
        content: content.value,
    });
    state.value = State.Display;
    if (!result.status)
        return;

    const newVersion = QueryVersion.fromServer(result.data, props.version.query);
    emit('createQueryVersion', newVersion);
}

const queryResult = ref<string>();
const isExecuting = ref(false);

async function execute() {
    queryResult.value = undefined;

    const response = await API.queries.execute({}, { categoryId: categoryInfo.value.id, queryString: content.value });
    const result = response.status
        ? response.data.rows.join(',\n')
        : 'Error :(\nname: ' + response.error.name + '\ndata:\n' + response.error.data;

    queryResult.value = result;
}
</script>

<template>
    <div class="query-display">
        <h2>{{ props.version.query.label }}</h2>
        <TextArea
            v-model="content"
            class="query-console"
            :disabled="state !== State.Editing"
        />
        <div class="d-flex align-items-center gap-3">
            <span>
                Version:
                <VersionDisplay
                    :version-id="version.version"
                    :error="isVersionOld"
                />
            </span>
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
            <button @click="execute">
                Execute
            </button>
        </div>
        <TextArea
            v-if="queryResult !== undefined"
            v-model="queryResult"
            class="query-console"
            readonly
        />
    </div>
</template>

<style scoped>
.query-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
}

.query-console {
    min-width: 600px;
}
</style>
