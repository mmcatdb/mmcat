import type { RouteRecordRaw } from 'vue-router';

export const projectIndependentRoutes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'home',
        component: () => import('@/views/project-independent/HomeView.vue')
    },
    {
        path: 'about',
        name: 'about',
        component: () => import('@/views/project-independent/AboutView.vue')
    },
    {
        path: 'databases',
        name: 'databases',
        component: () => import('@/views/project-specific/DatabasesView.vue')
    },
    {
        path: 'databases/:id',
        name: 'database',
        component: () => import('@/views/project-independent/DatabaseView.vue')
    },
];
