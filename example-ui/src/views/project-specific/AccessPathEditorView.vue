<script lang="ts">
import { defineComponent } from 'vue';

import AccessPathCreator from '@/components/accessPath/AccessPathCreator.vue';

export default defineComponent({
    components: {
        AccessPathCreator
    },
    props: {},
    data() {
        return {

        };
    },
    methods: {

    }
});
</script>

<template>
    <h1>Create Mapping</h1>
    <AccessPathCreator />
</template>

<style scoped>

</style>
