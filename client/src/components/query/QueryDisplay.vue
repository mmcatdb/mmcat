<script setup lang="ts">
import { QueryVersion } from '@/types/query';
import TextArea from '@/components/input/TextArea.vue';
import VersionDisplay from '../VersionDisplay.vue';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { computed, ref } from 'vue';
import API from '@/utils/api';
import OpenCloseToggle from '@/components/common/OpenCloseToggle.vue';

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
const isError = ref(false);
const isExecuting = ref(false);
const isOpened = ref(false);

async function execute() {
    queryResult.value = undefined;

    const response = await API.queries.execute({}, { categoryId: categoryInfo.value.id, queryString: content.value });
    const result = response.status
        ? response.data.rows.join(',\n')
        : 'Error :(\nname: ' + response.error.name + '\ndata:\n' + response.error.data;

    isError.value = !response.status;

    queryResult.value = result;
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
                    <button @click="execute">
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
            <TextArea
                v-if="queryResult !== undefined"
                v-model="queryResult"
                class="w-100"
                :class="{ 'text-danger': isError }"
                readonly
            />
        </template>
    </div>
</template>
