<script lang="ts">
import { defineComponent } from 'vue';
import InstanceCategoryDisplay from '../components/category/InstanceCategoryDisplay.vue';

export default defineComponent({
    components: {
        InstanceCategoryDisplay
    },
});
</script>

<template>
    <div>
        <h1>This is an instance category page</h1>
        <InstanceCategoryDisplay />
    </div>
</template>

<style scoped>
</style>
