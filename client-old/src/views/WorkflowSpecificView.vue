<script setup lang="ts">
import VersionDisplay from '@/components/VersionDisplay.vue';
import type { Id } from '@/types/id';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import { categoryInfoKey, workflowKey } from '@/utils/injects';
import { onMounted, provide, shallowRef, type Ref } from 'vue';
import { RouterView } from 'vue-router';
import SessionSelect from '@/components/SessionSelect.vue';
import type { Workflow } from '@/types/workflow';
import WorkflowSpecificNavigation from '@/components/layout/navigation/WorkflowSpecificNavigation.vue';
import { useFixedRouter } from '@/router/specificRoutes';

const props = defineProps<{
    workflowId: Id;
}>();

const schemaCategoryInfo = shallowRef<SchemaCategoryInfo>();
provide(categoryInfoKey, schemaCategoryInfo as Ref<SchemaCategoryInfo>);

const workflow = shallowRef<Workflow>();
provide(workflowKey, workflow as Ref<Workflow>);

const router = useFixedRouter();

onMounted(async () => {
    const workflowResult = await API.workflows.getWorkflow({ id: props.workflowId });
    if (!workflowResult.status) {
        router.push({ name: 'notFound' });
        return;
    }

    const categoryResult = await API.schemas.getCategoryInfo({ id: workflowResult.data.categoryId });
    if (!categoryResult.status) {
        router.push({ name: 'notFound' });
        return;
    }
    
    workflow.value = workflowResult.data;
    schemaCategoryInfo.value = SchemaCategoryInfo.fromServer(categoryResult.data);
});
</script>

<template>
    <template v-if="workflow && schemaCategoryInfo">
        <RouterView />
        <Teleport to="#app-top-bar-center">
            <h2>{{ schemaCategoryInfo.label }}</h2>
            <div class="ms-3">
                <span class="fw-semibold">v.</span>
                <VersionDisplay :version-id="schemaCategoryInfo.systemVersionId" />
            </div>
            <div class="ms-3">
                <SessionSelect :category-id="schemaCategoryInfo.id" />
            </div>
        </Teleport>
        <Teleport to="#app-left-bar-content">
            <WorkflowSpecificNavigation />
        </Teleport>
    </template>
</template>
