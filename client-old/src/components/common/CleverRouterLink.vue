<script setup lang="ts">
import type { Id } from '@/types/id';
import { useRoute, type RouteLocationNormalizedLoaded, type RouteLocationRaw } from 'vue-router';

function compareRoutes(to: RouteLocationRaw, route: RouteLocationNormalizedLoaded): boolean {
    if (typeof to === 'string')
        return to === route.fullPath;
    else if ('path' in to)
        return to.path === route.path;
    else if ('name' in to)
        return to.name === route.name;
    else return false;
}

function addWorkflowId(to: RouteLocationRaw, route: RouteLocationNormalizedLoaded): RouteLocationRaw {
    if (workflowId === undefined || typeof to === 'string')
        return to;

    return {
        ...to,
        query: {
            ...to.query,
            workflowId,
        },
    };
}
/**
 * This component looks like a normal link except for the situation when we are on the exact page the link directs to. Then only a plain content without any link is rendered.
 */
const props = defineProps<{ to: RouteLocationRaw }>();

const route = useRoute();


const workflowId = (route.query.workflowId ?? undefined) as Id | undefined;
</script>

<template>
    <RouterLink
        v-if="!compareRoutes(props.to, route)"
        :to="addWorkflowId(props.to, route)"
    >
        <slot />
    </RouterLink>
    <slot v-else />
</template>
