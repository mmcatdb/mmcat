<script setup lang="ts">
import type { Id } from '@/types/id';
import { Session } from '@/types/job';
import API from '@/utils/api';
import { onMounted, shallowRef } from 'vue';
import cookies from '@/utils/cookies';

const SESSION_KEY = 'session';

const props = defineProps<{
    categoryId: Id;
}>();

const sessions = shallowRef<Session[]>([]);
const selected = shallowRef<Session>();

onMounted(async () => {
    const sessionsResult = await API.jobs.getAllSessionsInCategory({ categoryId: props.categoryId });
    if (!sessionsResult.status) {
        // TODO handle error
        return;
    }

    sessions.value = sessionsResult.data.map(Session.fromServer).sort((a, b) => +a.createdAt - +b.createdAt);
    const cookieId = cookies.get(SESSION_KEY);

    const sessionFromCookie = sessions.value.find(s => s.id === cookieId);
    if (sessionFromCookie) {
        selected.value = sessionFromCookie;
        return;
    }

    selected.value = sessions.value[0];
    cookies.set(SESSION_KEY, selected.value.id);
});

function input(event: Event) {
    if (!sessions.value)
        return;

    const id = (event.target as unknown as { value?: string })?.value;
    const newSession = sessions.value.find(s => s.id === id);
    if (!newSession)
        return;

    selected.value = newSession;
    cookies.set(SESSION_KEY, selected.value.id);
}


async function createSession() {
    const result = await API.jobs.createSession({ categoryId: props.categoryId });
    if (!result.status) {
        // TODO handle error
        return;
    }

    const session = Session.fromServer(result.data);
    sessions.value = [ session, ...sessions.value ];
    selected.value = session;
    cookies.set(SESSION_KEY, selected.value.id);
}
</script>

<template>
    <div class="d-flex align-items-center gap-3">
        <div>
            <span class="fw-semibold me-2">Session:</span>
            <select
                class="reset-select"
                :value="selected?.id"
                @input="input"
            >
                <option
                    v-for="session in sessions"
                    :key="session.id"
                    :value="session.id"
                >
                    {{ session.id }}
                </option>
            </select>
        </div>
        <div>
            <span class="fw-semibold me-2">Since:</span>
            <span>{{ selected?.createdAt.toISOString() }}</span>
        </div>
        <button
            @click="createSession"
        >
            New session
        </button>
    </div>
</template>

<style scoped>
.reset-select {
    color: inherit;
    outline: none;
    background-color: transparent;
    border: none;
    padding: 0;
    margin: 0;
    font: inherit;
    cursor: pointer;
}

.reset-select option {
    background-color: var(--color-background);
}
</style>
