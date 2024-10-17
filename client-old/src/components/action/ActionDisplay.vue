<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import { Action, ActionType } from '@/types/action';
import CleverRouterLink from '@/components/common/CleverRouterLink.vue';
import ValueContainer from '@/components/layout/page/ValueContainer.vue';
import ValueRow from '@/components/layout/page/ValueRow.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';

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
        <CleverRouterLink :to="{ name: 'action', params: { id: action.id } }">
            <h2>{{ action.label }}</h2>
        </CleverRouterLink>
        <ValueContainer>
            <ValueRow label="Id:">
                {{ action.id }}
            </ValueRow>
            <ValueRow label="Type:">
                {{ action.payload.type }}
            </ValueRow>
            <ValueRow
                v-if="action.payload.type === ActionType.RSDToCategory "
                label="Datasource:"
            >
                <RouterLink :to="{ name: 'datasource', params: {id: action.payload.datasource.id }, query: { categoryId: action.categoryId } }">
                    {{ action.payload.datasource.label }}
                </RouterLink>
            </ValueRow>
            <ValueRow
                v-else-if="action.payload.type === ActionType.CategoryToModel || action.payload.type === ActionType.ModelToCategory"
                label="Logical model:"
            >
                <RouterLink :to="{ name: 'logicalModel', params: { id: action.payload.logicalModel.id } }">
                    {{ action.payload.logicalModel.label }}
                </RouterLink>
            </ValueRow>
            <ValueRow
                v-else-if="action.payload.type === ActionType.UpdateSchema"
                label="Versions:"
            >
                <!--  <VersionDisplay :version-id="action.payload.prevVersion" /> --> <VersionDisplay :version-id="action.payload.nextVersion" />
            </ValueRow>
            <!--
                <ValueRow label="State:">
                    <span :class="jobStateClass">
                        {{ action.state }}
                    </span>
                </ValueRow>
            <ValueRow
                v-if="action.state === JobState.Failed && action.data"
                label="Error:"
            >
                {{ action.data.name }}
            </ValueRow>
            <ValueRow v-else>
                &nbsp;
            </ValueRow>
        -->
        </ValueContainer>
        <div class="button-row">
            <button
                :disabled="fetching"
                class="success"
                @click="createRun"
            >
                Create job
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
    min-width: 284px;
}
</style>
