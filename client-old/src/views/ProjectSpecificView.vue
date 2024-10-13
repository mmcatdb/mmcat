<script setup lang="ts">
import NavigationContent from '@/components/layout/project-specific/NavigationContent.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import type { Id } from '@/types/id';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import { categoryIdKey, categoryInfoKey } from '@/utils/injects';
import { onMounted, provide, ref, shallowRef, type Ref } from 'vue';
import { RouterView, useRouter } from 'vue-router';
import SessionSelect from '@/components/SessionSelect.vue';

const props = defineProps<{
    categoryId: Id;
}>();

provide(categoryIdKey, ref(props.categoryId));

const schemaCategoryInfo = shallowRef<SchemaCategoryInfo>();

provide(categoryInfoKey, schemaCategoryInfo as Ref<SchemaCategoryInfo>);

const router = useRouter();

onMounted(async () => {
    const result = await API.schemas.getCategoryInfo({ id: props.categoryId });
    if (!result.status) {
        router.push({ name: 'notFound' });
        return;
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
            <NavigationContent />
        </Teleport>
    </template>
</template>
