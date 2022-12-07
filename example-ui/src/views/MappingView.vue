<script lang="ts">
import { defineComponent } from 'vue';
import API from '@/utils/api';

import ResourceNotFound from '@/components/ResourceNotFound.vue';
import ResourceLoading from '@/components/ResourceLoading.vue';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';
import { Mapping } from '@/types/mapping';

export default defineComponent({
    components: {
        ResourceNotFound,
        ResourceLoading,
        MappingDisplay
    },
    props: {},
    data() {
        return {
            mapping: null as Mapping | null,
            fetched: false
        };
    },
    async mounted() {
        const result = await API.mappings.getMapping({ id: this.$route.params.id });
        if (result.status)
            this.mapping = Mapping.fromServer(result.data);

        this.fetched = true;
    },
    methods: {

    }
});
</script>

<template>
    <h1>Mapping</h1>
    <div
        v-if="mapping"
        class="mapping"
    >
        <MappingDisplay
            :mapping="mapping"
        />
    </div>
    <ResourceNotFound v-else-if="fetched" />
    <ResourceLoading v-else />
</template>

<style scoped>
.mapping {
    display: flex;
}
</style>
