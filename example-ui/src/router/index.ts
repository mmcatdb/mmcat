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
            path: '/jobs/new',
            name: 'newJob',
            component: () => import('@/views/NewJobView.vue')
        },
        {
            path: '/jobs/:id',
            name: 'job',
            component: () => import('@/views/JobView.vue')
        },
        {
            path: '/accessPathEditor',
            name: 'accessPathEditor',
            component: () => import('@/views/AccessPathEditorView.vue')
        },
        {
            path: '/schema',
            name: 'schema',
            component: () => import('@/views/SchemaCategoryView.vue')
        },
        {
            path: '/instances',
            name: 'instances',
            component: () => import('@/views/InstancesView.vue')
        },
        {
            path: '/test',
            name: 'test',
            component: () => import('@/views/LongPageForTestsView.vue')
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
