<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import type { Action } from '@/types/action';
import FixedRouterLink from '@/components/common/FixedRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import JobPayloadDisplay from '../job/JobPayloadDisplay.vue';

type ActionDisplayProps = {
    action: Action;
};

const props = defineProps<ActionDisplayProps>();

const emit = defineEmits<{
    (e: 'deleteAction'): void;
}>();

const fetching = ref(false);

async function createRun() {
    fetching.value = true;

    // const result = await API.jobs.createRun({ id: props.action.id });
    await API.jobs.createRun({ actionId: props.action.id });
    // if (result.status)
    // TODO do something

    fetching.value = false;
}

async function deleteAction() {
    fetching.value = true;

    const result = await API.actions.deleteAction({ id: props.action.id });
    if (result.status)
        emit('deleteAction');

    fetching.value = false;
}
</script>

<template>
    <div class="action-display">
        <FixedRouterLink :to="{ name: 'action', params: { id: action.id } }">
            <h2>{{ action.label }}</h2>
        </FixedRouterLink>
        <div>
            <div><span class="text-bold me-2">Id:</span> {{ action.id }}</div>
        </div>
        <div>
            <div
                v-for="(payload, index) in action.payloads"
                :key="index"
                class="d-flex mt-1"
            >
                <div
                    class="mx-2 my-1 rounded-circle bg-white align-self-stretch"
                    :style="{ width: '2px' }"
                />
                <div>
                    <span class="text-bold me-2">Type:</span> {{ payload.type }}
                    <JobPayloadDisplay
                        :payload="payload"
                        with-labels
                    />
                </div>
            </div>
        </div>
        <div class="button-row">
            <button
                :disabled="fetching"
                class="success"
                @click="createRun"
            >
                Create run
            </button>
            <button
                :disabled="fetching"
                class="error"
                @click="deleteAction"
            >
                Delete
            </button>
        </div>
    </div>
</template>

<style scoped>
.action-display {
    padding: 12px;
    border: 1px solid var(--color-primary);
    margin-right: 16px;
    margin-bottom: 16px;
    width: 420px;
}
</style>
