<script lang="ts">
import { defineComponent } from 'vue';
import API from '@/utils/api';

import { Mapping } from '@/types/mapping';
import MappingDisplay from '@/components/accessPath/MappingDisplay.vue';
import ResourceLoader from '@/components/ResourceLoader.vue';


export default defineComponent({
    components: {
        ResourceLoader,
        MappingDisplay
    },
    props: {},
    data() {
        return {
            mapping: null as Mapping | null
        };
    },
    methods: {
        async fetchMapping() {
            const result = await API.mappings.getMapping({ id: this.$route.params.id });
            if (!result.status)
                return false;

            this.mapping = Mapping.fromServer(result.data);
            return true;
        }
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
    <ResourceLoader :loading-function="fetchMapping" />
</template>

<style scoped>
.mapping {
    display: flex;
}
</style>
