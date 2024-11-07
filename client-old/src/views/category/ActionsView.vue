<script setup lang="ts">
import { shallowRef } from 'vue';
import API from '@/utils/api';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import NewAction from '@/components/action/NewAction.vue';
import { useSchemaCategoryId } from '@/utils/injects';
import type { Id } from '@/types/id';
import ActionDisplay from '@/components/action/ActionDisplay.vue';
import { Action } from '@/types/action';

const actions = shallowRef<Action[]>();

function addNewAction(action: Action) {
    actions.value = [ ...(actions.value ?? []), action ];
}

function deleteAction(id: Id) {
    actions.value = actions.value?.filter(action => action.id !== id) ?? [];
}

const categoryId = useSchemaCategoryId();

async function fetchActions() {
    const result = await API.actions.getAllActionsInCategory({ categoryId });
    if (!result.status)
        return false;

    actions.value = result.data.map(Action.fromServer);
    return true;
}
</script>

<template>
    <div>
        <h1>Actions</h1>
        <div
            v-if="actions"
            class="d-flex flex-wrap"
        >
            <NewAction
                :key="actions.length"
                @new-action="addNewAction"
            />
            <div
                v-for="action in actions"
                :key="action.id"
            >
                <ActionDisplay
                    :action="action"
                    @delete-action="() => deleteAction(action.id)"
                />
            </div>
        </div>
        <ResourceLoader :loading-function="fetchActions" />
    </div>
</template>
