import { type NavigationFailure, useRoute, type RouteLocationNormalizedLoaded, type RouteLocationRaw, type RouteRecordRaw, useRouter } from 'vue-router';

const commonRoutes: Record<string, RouteRecordRaw> = {
    datasource: {
        path: 'datasources/:id',
        name: 'datasource',
        component: () => import('@/views/common/DatasourceView.vue'),
    },
    mapping: {
        path: 'mappings/:id',
        name: 'mapping',
        component: () => import('@/views/common/MappingView.vue'),
    },
    accessPathEditor: {
        path: 'mappings/new',
        name: 'accessPathEditor',
        component: () => import('@/views/common/AccessPathEditorView.vue'),
    },
};

const categoryRoutes: RouteRecordRaw[] = [ {
    path: 'actions',
    name: 'actions',
    component: () => import('@/views/category/ActionsView.vue'),
}, {
    path: 'actions/:id',
    name: 'action',
    component: () => import('@/views/category/ActionView.vue'),
}, {
    path: 'jobs',
    name: 'jobs',
    component: () => import('@/views/category/JobsView.vue'),
}, {
    path: 'jobs/:id',
    name: 'job',
    component: () => import('@/views/category/JobView.vue'),
},
commonRoutes.mapping,
commonRoutes.accessPathEditor,
{
    path: 'schema-category',
    name: 'schemaCategory',
    component: () => import('@/views/category/SchemaCategoryView.vue'),
}, {
    path: 'instance-category',
    name: 'instanceCategory',
    component: () => import('@/views/category/InstanceCategoryView.vue'),
}, {
    path: 'datasources',
    name: 'datasources',
    component: () => import('@/views/category/DatasourcesView.vue'),
},
commonRoutes.datasource,
{
    path: 'query',
    name: 'query',
    component: () => import('@/views/category/QueryingView.vue'),
}, {
    path: 'saved-queries',
    name: 'savedQueries',
    component: () => import('@/views/category/SavedQueriesView.vue'),
}, {
    path: '404',
    name: 'specificNotFound',
    component: () => import('@/views/common/PageNotFoundView.vue'),
}, {
    path: ':catchAll(.*)',
    redirect: { name: 'specificNotFound' },
} ];

const workflowRoutes: RouteRecordRaw[] = [ {
    path: '',
    name: 'index',
    component: () => import('@/views/workflow/IndexView.vue'),
},
commonRoutes.datasource,
commonRoutes.mapping,
commonRoutes.accessPathEditor,
];

// The specific routes can be accessed in two contexts: category and workflow. Therefore, they have to have different names.
// We take care of this by prepending the view type to the route name.
// The same is done in the FixedRouterLink component.

const viewTypes = [
    'category',
    'workflow',
] as const;

export type ViewType = typeof viewTypes[number];

function getSpecificRouteName(viewType: ViewType, name: string): string {
    const prefix = `${viewType}:`;
    return (name.startsWith(prefix) ? '' : prefix) + name;
}

function createSpecificRoutes(routes: RouteRecordRaw[], viewType: ViewType): RouteRecordRaw[] {
    return routes.map(route => ({
        ...route,
        name: route.name && getSpecificRouteName(viewType, route.name as string),
    }));
}

export const specificRoutes: Record<ViewType, RouteRecordRaw[]> = {
    category: createSpecificRoutes(categoryRoutes, 'category'),
    workflow: createSpecificRoutes(workflowRoutes, 'workflow'),
};

/**
 * Allows the routes from some view to be used in another view.
 * E.g., the workflow view contains the "datasource" route which can redirect to the "datasources" throught the "back button". However, the workflow view doesn't have the "datasources" route - it renders the datasources directly from index. So, we need to redirect the "datasource" route to the "index" route.
 */
const redirects: Record<ViewType, Record<string, string>> = {
    category: {},
    workflow: {
        'datasources': 'index',
        'job': 'index',
    },
};

// Routing

function tryGetRouteView(route: RouteLocationNormalizedLoaded): ViewType | undefined {
    if (typeof route.name !== 'string')
        return;

    for (const viewType of viewTypes) {
        if (route.name.startsWith(viewType + ':'))
            return viewType;
    }
}

/**
 * Fixes the route name by prepending the view type (if we are in a specific view context).
 * @param to The new route.
 * @param newView If provided, the view type will be taken from this instead of the current route.
 */
export function fixRouterName(to: RouteLocationRaw, route: RouteLocationNormalizedLoaded, newView?: ViewType): RouteLocationRaw {
    if (typeof to === 'string' || !('name' in to) || typeof to.name !== 'string')
        return to;

    const view = newView ?? tryGetRouteView(route);
    if (!view)
        return to;

    let fixedName = getSpecificRouteName(view, to.name);

    // Let's check if the route exists.
    if (!specificRoutes[view].find(r => r.name === fixedName)) {
        // Hmm, it doesn't. Let's try to redirect it.
        const redirect = redirects[view][to.name];
        if (!redirect)
            throw new Error(`Route "${fixedName}" does not exist and the original route "${to.name}" can't be redirected.`);

        fixedName = getSpecificRouteName(view, redirect);
    }

    return {
        ...to,
        name: fixedName,
    };
}

type FixedRouter = {
    push(to: RouteLocationRaw, newView?: ViewType): Promise<NavigationFailure | void | undefined>;
};

export function useFixedRouter(): FixedRouter {
    const route = useRoute();
    const router = useRouter();

    return {
        push(to: RouteLocationRaw, newView?: ViewType): Promise<NavigationFailure | void | undefined> {
            return router.push(fixRouterName(to, route, newView));
        },
    };
}
