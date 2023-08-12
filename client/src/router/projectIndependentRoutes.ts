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
        path: 'databases',
        name: 'databases',
        component: () => import('@/views/project-independent/DatabasesView.vue'),
    },
    {
        path: 'databases/:id',
        name: 'database',
        component: () => import('@/views/project-independent/DatabaseView.vue'),
    },
    {
        path: 'data-sources',
        name: 'dataSources',
        component: () => import('@/views/project-independent/DataSourcesView.vue'),
    },
    {
        path: 'data-sources/:id',
        name: 'dataSource',
        component: () => import('@/views/project-independent/DataSourceView.vue'),
    },
];
