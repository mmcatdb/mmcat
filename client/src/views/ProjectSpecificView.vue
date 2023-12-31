<script setup lang="ts">
import NavigationContent from '@/components/layout/project-specific/NavigationContent.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import type { Id } from '@/types/id';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import { categoryIdKey, categoryInfoKey } from '@/utils/injects';
import { onMounted, provide, ref, type Ref } from 'vue';
import { RouterView, useRouter } from 'vue-router';

type ProjectSpecificViewProps = {
    categoryId: Id;
};

const props = defineProps<ProjectSpecificViewProps>();

provide(categoryIdKey, ref(props.categoryId));

const schemaCategoryInfo = ref<SchemaCategoryInfo>();

provide(categoryInfoKey, schemaCategoryInfo as Ref<SchemaCategoryInfo>);

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
            <div class="version-display-outer">
                v. <VersionDisplay :version-id="schemaCategoryInfo.versionId" />
            </div>
        </Teleport>
        <Teleport to="#app-left-bar-content">
            <NavigationContent />
        </Teleport>
    </template>
</template>

<style>
.version-display-outer {
    margin-left: 16px;
}
</style>
