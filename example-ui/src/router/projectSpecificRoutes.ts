import type { RouteRecordRaw } from 'vue-router';

export const projectSpecificRoutes: RouteRecordRaw[] = [
    {
        path: 'jobs',
        name: 'jobs',
        component: () => import('@/views/project-specific/JobsView.vue')
    },
    {
        path: 'jobs/:id',
        name: 'job',
        component: () => import('@/views/project-specific/JobView.vue')
    },
    {
        path: 'logical-models',
        name: 'logicalModels',
        component: () => import('@/views/project-specific/LogicalModelsView.vue')
    },
    {
        path: 'logical-models/:id',
        name: 'logicalModel',
        component: () => import('@/views/project-specific/LogicalModelView.vue')
    },
    {
        path: 'logical-models/new',
        name: 'newLogicalModel',
        component: () => import('@/views/project-specific/NewLogicalModel.vue')
    },
    {
        path: 'mappings/:id',
        name: 'mapping',
        component: () => import('@/views/project-specific/MappingView.vue')
    },
    {
        path: 'mappings/new',
        name: 'accessPathEditor',
        component: () => import('@/views/project-specific/AccessPathEditorView.vue')
    },
    {
        path: 'schema-category',
        name: 'schemaCategory',
        component: () => import('@/views/project-specific/SchemaCategoryView.vue')
    },
    {
        path: 'instance-category',
        name: 'instanceCategory',
        component: () => import('@/views/project-specific/InstanceCategoryView.vue')
    },
    {
        path: 'models',
        name: 'models',
        component: () => import('@/views/project-specific/ModelsView.vue')
    },
    {
        path: 'models/:jobId',
        name: 'model',
        component: () => import('@/views/project-specific/ModelView.vue')
    },
    {
        path: 'databases',
        name: 'databasesInSchema',
        component: () => import('@/views/project-specific/DatabasesView.vue')
    },
    {
        path: '404',
        name: 'platformSpecificNotFound',
        component: () => import('@/views/PageNotFoundView.vue')
    },
    {
        path: ':catchAll(.*)',
        redirect: { name: 'platformSpecificNotFound' }
    }
];
