import { type NavigationFailure, useRoute, type RouteLocationNormalizedLoaded, type RouteLocationRaw, type RouteRecordRaw, useRouter } from 'vue-router';

const specificRoutes: RouteRecordRaw[] = [ {
    path: 'actions',
    name: 'actions',
    component: () => import('@/views/specific/ActionsView.vue'),
}, {
    path: 'actions/:id',
    name: 'action',
    component: () => import('@/views/specific/ActionView.vue'),
}, {
    path: 'jobs',
    name: 'jobs',
    component: () => import('@/views/specific/JobsView.vue'),
}, {
    path: 'jobs/:id',
    name: 'job',
    component: () => import('@/views/specific/JobView.vue'),
}, {
    path: 'logical-models',
    name: 'logicalModels',
    component: () => import('@/views/specific/LogicalModelsView.vue'),
}, {
    path: 'logical-models/:id',
    name: 'logicalModel',
    component: () => import('@/views/specific/LogicalModelView.vue'),
}, {
    path: 'logical-models/new',
    name: 'newLogicalModel',
    component: () => import('@/views/specific/NewLogicalModel.vue'),
}, {
    path: 'mappings/:id',
    name: 'mapping',
    component: () => import('@/views/specific/MappingView.vue'),
}, {
    path: 'mappings/new',
    name: 'accessPathEditor',
    component: () => import('@/views/specific/AccessPathEditorView.vue'),
}, {
    path: 'schema-category',
    name: 'schemaCategory',
    component: () => import('@/views/specific/SchemaCategoryView.vue'),
}, {
    path: 'instance-category',
    name: 'instanceCategory',
    component: () => import('@/views/specific/InstanceCategoryView.vue'),
}, {
    path: 'models',
    name: 'models',
    component: () => import('@/views/specific/ModelsView.vue'),
}, {
    path: 'models/:jobId',
    name: 'model',
    component: () => import('@/views/specific/ModelView.vue'),
}, {
    path: 'datasources',
    name: 'datasources',
    component: () => import('@/views/specific/DatasourcesInCategoryView.vue'),
}, {
    path: 'datasources/:id',
    name: 'datasource',
    component: () => import('@/views/independent/DatasourceView.vue'),
}, {
    path: 'query',
    name: 'query',
    component: () => import('@/views/specific/QueryingView.vue'),
}, {
    path: 'saved-queries',
    name: 'savedQueries',
    component: () => import('@/views/specific/SavedQueriesView.vue'),
}, {
    path: '404',
    name: 'specificNotFound',
    component: () => import('@/views/PageNotFoundView.vue'),
}, {
    path: ':catchAll(.*)',
    redirect: { name: 'specificNotFound' },
} ];

// The specific routes can be accessed in two contexts: category and workflow. Therefore, they have to have different names.
// We take care of this by prepending the view type to the route name.
// The same is done in the FixedRouterLink component.

const viewTypes = [
    'category',
    'workflow',
] as const;

export type ViewType = typeof viewTypes[number];

function getSpecificRouteName(viewType: ViewType, name: string): string {
    return `${viewType}:${name}`;
}

function createSpecificRoutes(viewType: ViewType): RouteRecordRaw[] {
    return specificRoutes.map(route => ({
        ...route,
        name: getSpecificRouteName(viewType, route.name as string),
    }));
}

export const categorySpecificRoutes: RouteRecordRaw[] = createSpecificRoutes('category');

export const workflowSpecificRoutes: RouteRecordRaw[] = createSpecificRoutes('workflow');

// Routing

const specificRouteNames: string[] = specificRoutes.map(route => route.name as string);

function tryGetRouteView(route: RouteLocationNormalizedLoaded): ViewType | undefined {
    if (typeof route.name !== 'string')
        return;

    for (const viewType of viewTypes)
        if (route.name.startsWith(viewType + ':'))
            return viewType;
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

    // The view prefix might be already applied.
    if (!specificRouteNames.includes(to.name))
        return to;

    return {
        ...to,
        name: getSpecificRouteName(view, to.name),
    };
}

type FixedRouter = {
    push(to: RouteLocationRaw, newView?: ViewType): Promise<NavigationFailure | void | undefined>;
}

export function useFixedRouter(): FixedRouter {
    const route = useRoute();
    const router = useRouter();

    return {
        push(to: RouteLocationRaw, newView?: ViewType): Promise<NavigationFailure | void | undefined> {
            return router.push(fixRouterName(to, route, newView));
        }
    }
}
