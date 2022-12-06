<script lang="ts">
import { defineComponent } from 'vue';

const LOADING_WAIT_TIME_IN_MILLISECONDS = 400;

export default defineComponent({
    data() {
        return {
            showLoadingMessage: false,
            timeoutId: undefined as number | undefined
        };
    },
    mounted() {
        this.timeoutId = setTimeout(() => {
            this.showLoadingMessage = true;
        }, LOADING_WAIT_TIME_IN_MILLISECONDS);
    },
    unmounted() {
        clearTimeout(this.timeoutId);
    }
});
</script>

<template>
    <p v-if="showLoadingMessage">
        Loading ....
    </p>
</template>
