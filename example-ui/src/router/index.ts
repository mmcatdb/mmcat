import { createRouter, createWebHistory, RouterView } from 'vue-router';
import { projectSpecificRoutes } from './projectSpecific';

import GeneralNavigationContent from '@/components/layout/project-independent/NavigationContent.vue';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            components: {
                default: () => import('@/views/project-independent/HomeView.vue'),
                leftBar: () => GeneralNavigationContent
            }
        },
        {
            path: '/:schemaCategoryId',
            name: 'projectSpecific',
            components: {
                default: () => import('@/views/ProjectSpecificView.vue'),
                leftBar: () => import('@/components/layout/project-specific/NavigationContent.vue')
            },
            children: projectSpecificRoutes
        },
        {
            path: '/404',
            name: 'notFound',
            components: {
                default: () => import('@/views/PageNotFoundView.vue'),
                leftBar: () => GeneralNavigationContent
            }
        },
        {
            path: '/:catchAll(.*)',
            redirect: { name: 'notFound' }
        }
    ]
});

export default router;
