<script setup lang="ts">
import IconArrowLeftBold from '../icons/IconArrowLeftBold.vue';
import { useRoute, useRouter } from 'vue-router';
import { toQueryScalar, createQueryParameterKeepingNavigationGuard } from '@/utils/router';
import { computed } from 'vue';

const router = useRouter();
router.beforeEach(createQueryParameterKeepingNavigationGuard('returnUrl'));
router.beforeEach(createQueryParameterKeepingNavigationGuard('returnUrlName'));

const route = useRoute();
const returnLink = computed(() => {
    const url = toQueryScalar(route.query.returnUrl);
    const name = toQueryScalar(route.query.returnUrlName);

    return url && name ? { url, name } : null;
});

function goBackToAtlas() {
    if (returnLink.value)
        window.location.href = returnLink.value.url;
}

console.log(route);
</script>

<template>
    <div
        v-if="returnLink"
        class="back-to-atlas"
    >
        <button @click="goBackToAtlas()">
            <IconArrowLeftBold class="icon" /> Back to {{ returnLink.name }}
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

.icon {
    position: relative;
    top: -3px;
}
</style>
