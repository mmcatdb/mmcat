<script lang="ts">
import { defineComponent } from 'vue';
import type { RouteLocationNormalizedLoaded, RouteLocationRaw } from 'vue-router';

function compareRoutes(to: RouteLocationRaw, route: RouteLocationNormalizedLoaded): boolean {
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
export default defineComponent({
    props: {
        to: {
            type: Object as () => RouteLocationRaw,
            required: true
        }
    },
    data() {
        return {
            showLink: !compareRoutes(this.to, this.$route)
        };
    }
});
</script>

<template>
    <RouterLink
        v-if="showLink"
        :to="to"
    >
        <slot />
    </RouterLink>
    <slot v-else />
</template>
