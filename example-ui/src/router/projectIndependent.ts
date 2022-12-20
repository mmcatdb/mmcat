export const projectIndependentRoutes = [
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
];
