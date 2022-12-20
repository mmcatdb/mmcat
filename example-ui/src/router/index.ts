import { createRouter, createWebHistory, RouterView } from 'vue-router';
import { projectSpecificRoutes } from './projectSpecific';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: () => import('@/views/HomeView.vue')
        },
        {
            path: '/:schemaCategoryId',
            name: 'projectSpecific',
            components: {
                default: () => import('@/views/ProjectSpecificView.vue'),
                //default: () => RouterView,
                leftBar: () => import('@/components/layout/project-specific/NavigationContent.vue')
            },
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
