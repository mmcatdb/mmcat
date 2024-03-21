<script setup lang="ts">
import NavigationContent from '@/components/layout/project-specific/NavigationContent.vue';
import VersionDisplay from '@/components/VersionDisplay.vue';
import type { Id } from '@/types/id';
import { Session } from '@/types/job';
import { SchemaCategoryInfo } from '@/types/schema';
import API from '@/utils/api';
import { categoryIdKey, categoryInfoKey } from '@/utils/injects';
import { onMounted, provide, ref, type Ref } from 'vue';
import { RouterView, useRouter } from 'vue-router';
import SessionSelect from '@/components/SessionSelect.vue';

type ProjectSpecificViewProps = {
    categoryId: Id;
};

const props = defineProps<ProjectSpecificViewProps>();

provide(categoryIdKey, ref(props.categoryId));

const schemaCategoryInfo = ref<SchemaCategoryInfo>();

provide(categoryInfoKey, schemaCategoryInfo as Ref<SchemaCategoryInfo>);

const sessions = ref<Session[]>([]);
const currentSession = ref<Session>();

const router = useRouter();

onMounted(async () => {
    const result = await API.schemas.getCategoryInfo({ id: props.categoryId });
    if (!result.status) {
        router.push({ name: 'notFound' });
        return;
    }

    schemaCategoryInfo.value = SchemaCategoryInfo.fromServer(result.data);

    const sessionsResult = await API.jobs.getAllSessionsInCategory({ categoryId: props.categoryId });
    if (!sessionsResult.status) {
        // TODO handle error
        return;
    }

    sessions.value = sessionsResult.data.map(Session.fromServer).sort((a, b) => +a.createdAt - +b.createdAt);
    currentSession.value = sessions.value[0];
});
</script>

<template>
    <template v-if="schemaCategoryInfo">
        <RouterView />
        <Teleport to="#app-top-bar-center">
            <h2>{{ schemaCategoryInfo.label }}</h2>
            <div class="ms-3">
                v. <VersionDisplay :version-id="schemaCategoryInfo.versionId" />
            </div>
            <div class="ms-3">
                <SessionSelect
                    :sessions="sessions"
                    :current-session="currentSession"
                />
            </div>
        </Teleport>
        <Teleport to="#app-left-bar-content">
            <NavigationContent />
        </Teleport>
    </template>
</template>
