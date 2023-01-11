import { createRouter, createWebHistory } from 'vue-router';
import { projectSpecificRoutes } from './projectSpecificRoutes';
import { projectIndependentRoutes } from './projectIndependentRoutes';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            component: () => import('@/views/ProjectIndependentView.vue'),
            children: projectIndependentRoutes
        },
        {
            path: '/integration',
            name: 'integration',
            component: () => import('@/views/IntegrationView.vue')
        },
        {
            path: '/:categoryId',
            component: () => import('@/views/ProjectSpecificView.vue'),
            props: route => ({ categoryId: route.params.categoryId }),
            children: projectSpecificRoutes
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
