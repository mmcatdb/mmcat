<script setup lang="ts">
import { fixRouterName, type ViewType } from '@/router/specificRoutes';
import { useRoute, type RouteLocationNormalizedLoaded, type RouteLocationRaw } from 'vue-router';

function compareRoutes(to: RouteLocationRaw, route: RouteLocationNormalizedLoaded): boolean {
    console.log('compare', to, route);
    if (typeof to === 'string')
        return to === route.fullPath;
    else if ('path' in to)
        return to.path === route.path;
    else if ('name' in to)
        return to.name === route.name;
    else return false;
}

/**
 * This component looks like a normal link except for the situation when we are on the exact page the link directs to. Then only a plain content without any link is rendered.
 */
const props = defineProps<{
    to: RouteLocationRaw;
    view?: ViewType;
    alwaysLink?: boolean;
}>();

const route = useRoute();
const fixedTo = fixRouterName(props.to, route, props.view);
</script>

<template>
    <RouterLink
        v-if="alwaysLink || !compareRoutes(fixedTo, $route)"
        :to="fixedTo"
    >
        <slot />
    </RouterLink>
    <slot v-else />
</template>
