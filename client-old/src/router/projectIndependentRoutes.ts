import type { RouteRecordRaw } from 'vue-router';

export const projectIndependentRoutes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'home',
        component: () => import('@/views/project-independent/HomeView.vue'),
    },
    {
        path: 'about',
        name: 'about',
        component: () => import('@/views/project-independent/AboutView.vue'),
    },
    {
        path: 'datasources',
        name: 'datasources',
        component: () => import('@/views/project-independent/DatasourcesView.vue'),
    },
    {
        path: 'datasources/:id',
        name: 'datasource',
        component: () => import('@/views/project-independent/DatasourceView.vue'),
    },
];
