<script setup lang="ts">
import NavigationContent from '@/components/layout/project-specific/NavigationContent.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import type { Id } from '@/types/id';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import { categoryIdKey, categoryInfoKey, useWorkflowId, workflowKey } from '@/utils/injects';
import { onMounted, provide, ref, shallowRef, type Ref } from 'vue';
import { type RouteLocationRaw, RouterView, useRouter } from 'vue-router';
import SessionSelect from '@/components/SessionSelect.vue';
import type { Workflow } from '@/types/workflow';
import WorkflowNavigationContent from '@/components/layout/project-specific/WorkflowNavigationContent.vue';

const props = defineProps<{
    categoryId: Id;
}>();

provide(categoryIdKey, ref(props.categoryId));

const schemaCategoryInfo = shallowRef<SchemaCategoryInfo>();
provide(categoryInfoKey, schemaCategoryInfo as Ref<SchemaCategoryInfo>);

const workflow = shallowRef<Workflow>();
provide(workflowKey, workflow);

const router = useRouter();

const workflowId = useWorkflowId();

function getInitialWorkflowRoute(workflow: Workflow): RouteLocationRaw {
    switch (workflow.data.type) {
    case 'inference':
        if (workflow.data.jobId) {
            return {
                name: 'job',
                params: { id: workflow.data.jobId },
                query: { workflowId: workflow.id },
            };
        }

        return {
            name: 'logicalModels',
            query: { workflowId: workflow.id },
        };
    }
}

onMounted(async () => {
    const result = await API.schemas.getCategoryInfo({ id: props.categoryId });
    if (!result.status) {
        router.push({ name: 'notFound' });
        return;
    }

    if (workflowId) {
        const workflowResult = await API.workflows.getWorkflow({ id: workflowId });
        if (!workflowResult.status) {
            router.push({ name: 'notFound' });
            return;
        }

        workflow.value = workflowResult.data;
        router.push(getInitialWorkflowRoute(workflowResult.data));
    }
    
    schemaCategoryInfo.value = SchemaCategoryInfo.fromServer(result.data);
});
</script>

<template>
    <template v-if="schemaCategoryInfo">
        <RouterView />
        <Teleport to="#app-top-bar-center">
            <h2>{{ schemaCategoryInfo.label }}</h2>
            <div class="ms-3">
                <span class="fw-semibold">v.</span>
                <VersionDisplay :version-id="schemaCategoryInfo.systemVersionId" />
            </div>
            <div class="ms-3">
                <SessionSelect
                    :category-id="categoryId"
                />
            </div>
        </Teleport>
        <Teleport to="#app-left-bar-content">
            <template v-if="workflow">
                <WorkflowNavigationContent />
                <button class="mt-4">
                    Continue
                </button>
            </template>
            <NavigationContent v-else />
        </Teleport>
    </template>
</template>
