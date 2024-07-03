import type { RouteRecordRaw } from 'vue-router';

export const projectSpecificRoutes: RouteRecordRaw[] = [
    {
        path: 'actions',
        name: 'actions',
        component: () => import('@/views/project-specific/ActionsView.vue'),
    },
    {
        path: 'actions/:id',
        name: 'action',
        component: () => import('@/views/project-specific/ActionView.vue'),
    },
    {
        path: 'jobs',
        name: 'jobs',
        component: () => import('@/views/project-specific/JobsView.vue'),
    },
    {
        path: 'jobs/:id',
        name: 'job',
        component: () => import('@/views/project-specific/JobView.vue'),
    },
    {
        path: 'logical-models',
        name: 'logicalModels',
        component: () => import('@/views/project-specific/LogicalModelsView.vue'),
    },
    {
        path: 'logical-models/:id',
        name: 'logicalModel',
        component: () => import('@/views/project-specific/LogicalModelView.vue'),
    },
    {
        path: 'logical-models/new',
        name: 'newLogicalModel',
        component: () => import('@/views/project-specific/NewLogicalModel.vue'),
    },
    {
        path: 'mappings/:id',
        name: 'mapping',
        component: () => import('@/views/project-specific/MappingView.vue'),
    },
    {
        path: 'mappings/new',
        name: 'accessPathEditor',
        component: () => import('@/views/project-specific/AccessPathEditorView.vue'),
    },
    {
        path: 'schema-category',
        name: 'schemaCategory',
        component: () => import('@/views/project-specific/SchemaCategoryView.vue'),
    },
    {
        path: 'instance-category',
        name: 'instanceCategory',
        component: () => import('@/views/project-specific/InstanceCategoryView.vue'),
    },
    {
        path: 'models',
        name: 'models',
        component: () => import('@/views/project-specific/ModelsView.vue'),
    },
    {
        path: 'models/:jobId',
        name: 'model',
        component: () => import('@/views/project-specific/ModelView.vue'),
    },
    {
        path: 'datasources',
        name: 'datasourcesInCategory',
        component: () => import('@/views/project-specific/DatasourcesInCategoryView.vue'),
    },
    {
        path: 'query',
        name: 'query',
        component: () => import('@/views/project-specific/QueryingView.vue'),
    },
    {
        path: 'saved-queries',
        name: 'savedQueries',
        component: () => import('@/views/project-specific/SavedQueriesView.vue'),
    },
    {
        path: '404',
        name: 'platformSpecificNotFound',
        component: () => import('@/views/PageNotFoundView.vue'),
    },
    {
        path: ':catchAll(.*)',
        redirect: { name: 'platformSpecificNotFound' },
    },
];
