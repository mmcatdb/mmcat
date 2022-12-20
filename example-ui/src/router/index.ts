import { createRouter, createWebHistory } from 'vue-router';
import { projectSpecificRoutes } from './projectSpecific';
import { projectIndependentRoutes } from './projectIndependent';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            component: () => import('@/views/ProjectIndependentView.vue'),
            children: projectIndependentRoutes
        },
        {
            path: '/:schemaCategoryId',
            component: () => import('@/views/ProjectSpecificView.vue'),
            props: route => ({ schemaCategoryId: route.params.schemaCategoryId }),
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
