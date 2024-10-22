<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

const LOADING_WAIT_TIME_IN_MILLISECONDS = 400;

type ResourceLoaderProps = {
    loadingFunction: () => Promise<boolean | 'no-refetch'>;
    notReloadable?: boolean;
    refreshPeriod?: number;
};

const props = defineProps<ResourceLoaderProps>();

enum State {
    Loading,
    LoadingVisible,
    Success,
    NotFound,
    Reloading
}

const state = ref(State.Loading);
const loadingTimeoutId = ref<number>();

onMounted(async () => {
    loadingTimeoutId.value = setTimeout(() => {
        if (state.value === State.Loading)
            state.value = State.LoadingVisible;
    }, LOADING_WAIT_TIME_IN_MILLISECONDS);

    onFinishedFetch(await props.loadingFunction());
});

onUnmounted(() => {
    clearTimeout(loadingTimeoutId.value);
    clearTimeout(refreshingTimeoutId.value);
    continueRefreshing.value = false;
});

async function reload() {
    state.value = State.Reloading;
    onFinishedFetch(await props.loadingFunction());
}

const refreshingTimeoutId = ref<number>();
// The reason for this variable is that we want to stop additional fetches while the fetchNew is in the middle of the fetching.
// So just clearing the timeout isn't enough.
// The correct solution would be to add an abort controller.
const continueRefreshing = ref(true);

function onFinishedFetch(result: boolean | 'no-refetch') {
    state.value = result ? State.Success : State.NotFound;

    if (result && result !== 'no-refetch' && props.refreshPeriod && continueRefreshing.value) {
        refreshingTimeoutId.value = setTimeout(async () => {
            onFinishedFetch(await props.loadingFunction());
        }, props.refreshPeriod);
    }
}

const stateTexts = {
    [State.Loading]: '',
    [State.LoadingVisible]: 'Loading ...',
    [State.NotFound]: 'Resource not found.',
    [State.Reloading]: 'Loading ...',
};
</script>

<template>
    <p
        v-if="state !== State.Success"
    >
        <span class="state-text">
            {{ stateTexts[state] }}
        </span>
        <button
            v-if="!props.notReloadable"
            class="reload-button"
            :class="{ hidden: state !== State.NotFound && state !== State.Reloading }"
            :disabled="state === State.Reloading"
            @click="reload"
        >
            Reload
        </button>
    </p>
</template>

<style scoped>
.reload-button {
    margin: 12px;
}

.state-text {
    display: inline-block;
    width: 200px;
}

.hidden {
    visibility: hidden;
}
</style>
