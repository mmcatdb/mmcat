import { createRouter, createWebHistory } from 'vue-router';
import { categorySpecificRoutes, workflowSpecificRoutes } from './specificRoutes';
import { independentRoutes } from './independentRoutes';

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [ {
        path: '/',
        component: () => import('@/views/IndependentView.vue'),
        children: independentRoutes,
    }, {
        path: '/categories/:categoryId',
        component: () => import('@/views/CategorySpecificView.vue'),
        props: route => ({ categoryId: route.params.categoryId }),
        children: categorySpecificRoutes,
    }, {
        path: '/workflows/:workflowId',
        component: () => import('@/views/WorkflowSpecificView.vue'),
        props: route => ({ workflowId: route.params.workflowId }),
        children: workflowSpecificRoutes,
    }, {
        path: '/404',
        name: 'notFound',
        component: () => import('@/views/PageNotFoundView.vue'),
    }, {
        path: '/:catchAll(.*)',
        redirect: { name: 'notFound' },
    } ],
});

export default router;
