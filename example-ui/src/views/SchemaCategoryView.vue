<script lang="ts">
import { defineComponent } from 'vue';
import SchemaCategoryEditor from '@/components/category/ScemaCategoryEditor.vue';

export default defineComponent({
    components: {
        SchemaCategoryEditor
    },
    props: {},
    data() {
        return {};
    }
});
</script>

<template>
    <h1>There is a schema category</h1>
    <SchemaCategoryEditor />
</template>

<style scoped>
#cytoscape {
    width: 1600px;
    height: 500px;
    background-color: whitesmoke;
}
</style>
