<script setup lang="ts">
import { ref, watch } from 'vue';
import API from '@/utils/api';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { queryWithVersionFromServer, type QueryWithVersion } from '@/types/query';

type SaveQueryButtonProps = {
    content: string;
};

const props = defineProps<SaveQueryButtonProps>();
const categoryInfo = useSchemaCategoryInfo();

const emit = defineEmits<{
    (e: 'createQuery', query: QueryWithVersion): void;
}>();

enum State { Default, Label, Fetching }
const state = ref<State>(State.Default);
const label = ref('');

watch(() => props.content, () => {
    state.value = State.Default;
    label.value = '';
});

async function finishSaving() {
    state.value = State.Fetching;
    const result = await API.queries.createQuery({}, {
        categoryId: categoryInfo.value.id,
        version: categoryInfo.value.versionId,
        label: label.value,
        content: props.content,
    });
    state.value = State.Default;
    if (result.status) {
        const query = queryWithVersionFromServer(result.data);
        emit('createQuery', query);
    }
}
</script>

<template>
    <div class="save-query-button">
        <template v-if="state === State.Default">
            <button @click="() => state = State.Label">
                Save
            </button>
        </template>
        <template v-else>
            <input
                v-model="label"
                :disabled="state === State.Fetching"
                type="text"
                class="label-input"
            />
            <button
                :disabled="state === State.Fetching"
                @click="finishSaving"
            >
                Finish
            </button>
        </template>
    </div>
</template>

<style scoped>
.save-query-button {
    padding: 12px;
    border: 1px solid var(--color-primary);
    min-width: 284px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.label-input {
    flex-grow: 1;
}
</style>
