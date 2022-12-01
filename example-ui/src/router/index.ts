import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
        },
        {
            path: '/jobs',
            name: 'jobs',
            component: () => import('@/views/JobsView.vue')
        },
        {
            path: '/jobs/:id',
            name: 'job',
            component: () => import('@/views/JobView.vue')
        },
        {
            path: '/logical-models',
            name: 'logicalModels',
            component: () => import('@/views/LogicalModelsView.vue')
        },
        {
            path: '/logical-models/:id',
            name: 'logicalModel',
            component: () => import('@/views/LogicalModelView.vue')
        },
        {
            path: '/logical-models/new',
            name: 'newLogicalModel',
            component: () => import('@/views/NewLogicalModel.vue')
        },
        /*
        {
            path: '/logical-models/new',
            name: 'accessPathEditor',
            component: () => import('@/views/AccessPathEditorView.vue')
        },
        */
        {
            path: '/mappings',
            name: 'mappings',
            component: () => import('@/views/MappingsView.vue')
        },
        {
            path: '/mappings/new',
            name: 'accessPathEditor',
            component: () => import('@/views/AccessPathEditorView.vue')
        },
        {
            path: '/schema-category',
            name: 'schemaCategory',
            component: () => import('@/views/SchemaCategoryView.vue')
        },
        {
            path: '/instance-category',
            name: 'instanceCategory',
            component: () => import('@/views/InstanceCategoryView.vue')
        },
        {
            path: '/models',
            name: 'models',
            component: () => import('@/views/ModelsView.vue')
        },
        {
            path: '/models/:jobId',
            name: 'model',
            component: () => import('@/views/ModelView.vue')
        },
        {
            path: '/databases',
            name: 'databases',
            component: () => import('@/views/DatabasesView.vue')
        },
        {
            path: '/databases/:id',
            name: 'database',
            component: () => import('@/views/DatabaseView.vue')
        },
        {
            path: '/404',
            name: 'notFound',
            component: () => import('@/views/PageNotFoundView.vue')
        },
        {
            path: '/:catchAll(.*)',
            redirect: { name: 'notFound' }
        }
    ]
});

export default router;
