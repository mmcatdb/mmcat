<script lang="ts">
import { defineComponent } from 'vue';

import SchemaCategoryGraph from '@/components/category/SchemaCategoryGraph.vue';

export default defineComponent({
    components: {
        SchemaCategoryGraph
    },
    props: {},
    data() {
        return {};
    }
});
</script>

<template>
    <h1>There is a schema category</h1>
    <SchemaCategoryGraph />
</template>

<style scoped>
#cytoscape {
    width: 1600px;
    height: 500px;
    background-color: whitesmoke;
}
</style>
