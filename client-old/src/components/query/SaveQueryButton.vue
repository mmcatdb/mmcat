<script setup lang="ts">
import { ref, watch } from 'vue';
import API from '@/utils/api';
import { useSchemaCategoryInfo } from '@/utils/injects';
import { Query } from '@/types/query';

type SaveQueryButtonProps = {
    content: string;
};

const props = defineProps<SaveQueryButtonProps>();
const categoryInfo = useSchemaCategoryInfo();

const emit = defineEmits<{
    (e: 'saveQuery', query: Query): void;
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
        label: label.value,
        content: props.content,
    });
    state.value = State.Default;
    if (result.status) {
        const query = Query.fromServer(result.data);
        emit('saveQuery', query);
    }
}

function cancelSaving() {
    state.value = State.Default;
    label.value = '';
}
</script>

<template>
    <div class="d-flex align-items-center gap-3">
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
                placeholder="Query label"
            />
            <button
                :disabled="state === State.Fetching || label === ''"
                @click="finishSaving"
            >
                Save
            </button>
            <button
                :disabled="state === State.Fetching"
                @click="cancelSaving"
            >
                Cancel
            </button>
        </template>
    </div>
</template>

<style scoped>
.label-input {
    flex-grow: 1;
}
</style>
