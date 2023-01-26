<script setup lang="ts">
import IconArrowLeftBold from '../icons/IconArrowLeftBold.vue';
import { useRoute, useRouter } from 'vue-router';
import { toQueryScalar, createQueryParameterKeepingNavigationGuard } from '@/utils/router';
import { computed } from 'vue';

const router = useRouter();
router.beforeEach(createQueryParameterKeepingNavigationGuard('pimIri'));

const route = useRoute();
const pimIri = computed(() => toQueryScalar(route.query.pimIri));
const DATASPECER_SITE_PREFIX_URL = import.meta.env.VITE_DATASPECER_SITE_PREFIX_URL;

function goBackToAtlas() {
    if (pimIri.value)
        window.location.href = DATASPECER_SITE_PREFIX_URL + pimIri.value;
}

console.log(route);
</script>

<template>
    <div
        v-if="pimIri"
        class="back-to-atlas"
    >
        <button @click="goBackToAtlas()">
            <IconArrowLeftBold /> Back to Atlas
        </button>
    </div>
</template>

<style scoped>
.back-to-atlas {
    order: 2;
    margin-left: 16px;
}

.back-to-atlas button {
    font-size: 22.5px;
    padding-right: 16px;
}
</style>
