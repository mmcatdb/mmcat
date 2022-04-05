<script lang="ts">
import { defineComponent } from 'vue';

import GraphDisplay from '@/components/category/GraphDisplay.vue';

export default defineComponent({
    components: {
        GraphDisplay
    },
    props: {},
    data() {
        return {};
    }
});
</script>

<template>
    <h1>There is a schema category</h1>
    <GraphDisplay />
</template>

<style scoped>
#cytoscape {
    width: 1600px;
    height: 500px;
    background-color: whitesmoke;
}
</style>
