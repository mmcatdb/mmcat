<script setup lang="ts">
import { ref } from 'vue';
import API from '@/utils/api';
import ResourceLoader from '@/components/common/ResourceLoader.vue';
import ActionDisplay from '@/components/action/ActionDisplay.vue';
import { useRoute } from 'vue-router';
import { Action } from '@/types/action';
import { useFixedRouter } from '@/router/specificRoutes';

const action = ref<Action>();

const route = useRoute();

async function fetchAction() {
    const result = await API.actions.getAction({ id: route.params.id });
    if (!result.status)
        return false;

    action.value = Action.fromServer(result.data);
    return true;
}

const router = useFixedRouter();

function deleteAction() {
    router.push({ name: 'actions' });
}
</script>

<template>
    <h1>Action</h1>
    <div
        v-if="action"
        class="d-flex"
    >
        <ActionDisplay
            :action="action"
            @delete-action="deleteAction"
        />
    </div>
    <ResourceLoader :loading-function="fetchAction" />
</template>
