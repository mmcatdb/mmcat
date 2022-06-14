<script lang="ts">
import { defineComponent } from 'vue';
import SchemaCategoryEditor from '@/components/category/SchemaCategoryEditor.vue';

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
    <h1>Schema Category</h1>
    <SchemaCategoryEditor />
</template>
