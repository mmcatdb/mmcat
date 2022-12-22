import { RouterView, type RouteRecordRaw } from 'vue-router';

export const projectSpecificRoutes: RouteRecordRaw[] = [
    {
        path: 'jobs',
        component: () => RouterView,
        children: [
            {
                path: '',
                name: 'jobs',
                component: () => import('@/views/project-specific/JobsView.vue')
            },
            {
                path: ':id',
                name: 'job',
                component: () => import('@/views/project-specific/JobView.vue')
            }
        ]
    },
    {
        path: 'logical-models',
        component: () => RouterView,
        children: [
            {
                path: '',
                name: 'logicalModels',
                component: () => import('@/views/project-specific/LogicalModelsView.vue'),
            },
            {
                path: 'new',
                name: 'newLogicalModel',
                component: () => import('@/views/project-specific/NewLogicalModel.vue')
            },
            {
                path: ':id',
                name: 'logicalModel',
                component: () => import('@/views/project-specific/LogicalModelView.vue')
            }
        ]
    },
    {
        path: 'mappings',
        component: () => RouterView,
        children: [
            {
                path: 'new',
                name: 'accessPathEditor',
                component: () => import('@/views/project-specific/AccessPathEditorView.vue')
            },
            {
                path: ':id',
                name: 'mapping',
                component: () => import('@/views/project-specific/MappingView.vue')
            }
        ]
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
        component: () => RouterView,
        children: [
            {
                path: '',
                name: 'models',
                component: () => import('@/views/project-specific/ModelsView.vue')
            },
            {
                path: ':jobId',
                name: 'model',
                component: () => import('@/views/project-specific/ModelView.vue')
            }
        ]
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
