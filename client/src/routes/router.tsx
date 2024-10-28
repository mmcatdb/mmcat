import { createBrowserRouter } from 'react-router-dom';
import { Home } from '@/pages/Home';
import { SchemaCategory, schemaCategoryLoader } from '@/pages/category/SchemaCategory';
import { About } from '@/pages/About';
import { routes } from '@/routes/routes';
import { ErrorPage } from '@/pages/errorPages';
import { CategoryIndex, categoryIndexLoader } from '@/pages/CategoryIndex';
import { DatasourcesPage, DatasourcesPageOverview } from '@/pages/DatasourcesPage';
import { DatasourceDetailPage } from '@/pages/DatasourceDetailPage';
import { AdminerPage } from '@/pages/AdminerPage';
import { SchemaCategoriesPage } from '@/pages/SchemaCategoriesPage';
import { ModelsPage } from '@/pages/ModelsPage';
import { QueryingPage } from '@/pages/QueryingPage';
import { RootLayout } from '@/components/RootLayout';

export const router = createBrowserRouter([
    {
        path: '/',
        Component: RootLayout,
        ErrorBoundary: ErrorPage,
        children: [
            {
                // id: routes.home.id,
                // path: routes.home.path,
                index: true,
                Component: Home,
            },
            {
                path: routes.categories,
                Component: SchemaCategoriesPage,
                handle: { breadcrumb: 'Schema Categories' },
            },
            {
                path: routes.about,
                Component: About,
                handle: { breadcrumb: 'About' },
            },
            {
                path: routes.datasources,
                Component: DatasourcesPage,
                handle: { breadcrumb: 'Datasources' },
                children: [
                    {
                        index: true,
                        Component: DatasourcesPageOverview,
                    },
                    {
                        path: ':id',
                        Component: DatasourceDetailPage,
                        handle: { breadcrumb: 'Detail' },
                    },
                ],
            },
            {
                path: routes.adminer,
                Component: AdminerPage,
                handle: { breadcrumb: 'Adminer' },
            },
            {
                path: routes.category.index.path,
                Component: CategoryIndex,
                loader: categoryIndexLoader,
                handle: { breadcrumb: 'Category' },
                children: [
                    {
                        index: true,
                        id: routes.category.index.id,
                        loader: schemaCategoryLoader,
                        Component: SchemaCategory,
                        // handle: { breadcrumb: 'Overview' },
                    },
                    {
                        id: routes.category.editor.id,
                        path: routes.category.editor.path,
                        loader: schemaCategoryLoader,
                        Component: SchemaCategory,
                        handle: { breadcrumb: 'Editor' },
                    },
                    {
                        id: routes.category.models.id,
                        path: routes.category.models.path,
                        Component: ModelsPage,
                        handle: { breadcrumb: 'Models' },
                    },
                    {
                        id: routes.category.querying.id,
                        path: routes.category.querying.path,
                        Component: QueryingPage,
                        handle: { breadcrumb: 'Querying' },
                    },
                ],
            },
            // catch-all route for 404 errors
            {
                path: '*',
                Component: ErrorPage,
            },
        ],

    },
]);
