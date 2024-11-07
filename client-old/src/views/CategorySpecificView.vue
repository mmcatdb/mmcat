<script setup lang="ts">
import CategorySpecificNavigation from '@/components/layout/navigation/CategorySpecificNavigation.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import type { Id } from '@/types/id';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import { categoryInfoKey } from '@/utils/injects';
import { onMounted, provide, shallowRef, type Ref } from 'vue';
import { RouterView } from 'vue-router';
import SessionSelect from '@/components/SessionSelect.vue';
import { useFixedRouter } from '@/router/specificRoutes';

const props = defineProps<{
    categoryId: Id;
}>();

const schemaCategoryInfo = shallowRef<SchemaCategoryInfo>();

provide(categoryInfoKey, schemaCategoryInfo as Ref<SchemaCategoryInfo>);

const router = useFixedRouter();

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
            <CategorySpecificNavigation />
        </Teleport>
    </template>
</template>
