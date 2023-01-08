<script setup lang="ts">
import NavigationContent from '@/components/layout/project-specific/NavigationContent.vue';
import type { Id } from '@/types/id';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import { onMounted, provide, ref } from 'vue';
import { RouterView, useRouter } from 'vue-router';

interface ProjectSpecificViewProps {
    categoryId: Id;
}

const props = defineProps<ProjectSpecificViewProps>();

provide('categoryId', props.categoryId);

const schemaCategoryInfo = ref<SchemaCategoryInfo>();
const router = useRouter();

onMounted(async () => {
    const result = await API.schemas.getCategoryInfo({ id: props.categoryId });
    if (result.status)
        schemaCategoryInfo.value = SchemaCategoryInfo.fromServer(result.data);
    else
        router.push({ name: 'notFound' });
});
</script>

<template>
    <template v-if="schemaCategoryInfo">
        <RouterView />
        <Teleport to="#app-top-bar-center">
            <h2>{{ schemaCategoryInfo.label }}</h2>
        </Teleport>
        <Teleport to="#app-left-bar-content">
            <NavigationContent />
        </Teleport>
    </template>
</template>
