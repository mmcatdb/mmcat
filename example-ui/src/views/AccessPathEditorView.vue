<script lang="ts">
import { defineComponent } from 'vue';

import AccessPathGraphicEditor from '@/components/accessPath/AccessPathGraphicEditor.vue';

export default defineComponent({
    components: {
        AccessPathGraphicEditor
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
    <h1>Access path editor</h1>
    <AccessPathGraphicEditor />
</template>

<style scoped>
.divide {
    display: flex;
}
</style>
