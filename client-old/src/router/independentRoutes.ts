import type { RouteRecordRaw } from 'vue-router';

export const independentRoutes: RouteRecordRaw[] = [ {
    path: '/',
    name: 'home',
    component: () => import('@/views/independent/HomeView.vue'),
}, {
    path: 'about',
    name: 'about',
    component: () => import('@/views/independent/AboutView.vue'),
}, {
    path: 'datasources',
    name: 'datasources',
    component: () => import('@/views/independent/DatasourcesView.vue'),
}, {
    path: 'datasources/:id',
    name: 'datasource',
    component: () => import('@/views/common/DatasourceView.vue'),
} ];
