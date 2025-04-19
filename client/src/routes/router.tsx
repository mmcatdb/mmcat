import { createBrowserRouter } from 'react-router-dom';
import { Home } from '@/pages/Home';
import { CategoryEditorPage } from '@/pages/category/CategoryEditorPage';
import { routes } from '@/routes/routes';
import { ErrorPage } from '@/pages/errorPages';
import { CategoryPage, type CategoryLoaderData } from '@/pages/CategoryPage';
import { DatasourcesPage } from '@/pages/DatasourcesPage';
import { type DatasourceLoaderData, DatasourceDetailPage, DatasourceInCategoryPage } from '@/pages/DatasourcePage';
// import { AdminerPage } from '@/pages/AdminerPage';
import { CategoriesPage, SchemaCategoriesPage } from '@/pages/CategoriesPage';
import { RootLayout } from '@/components/RootLayout';
import { DatasourcesInCategoryPage } from '@/pages/category/DatasourcesInCategoryPage';
import { ActionsPage } from '@/pages/category/ActionsPage';
import { AddActionPage } from '@/pages/category/AddActionPage';
import { CategoryOverviewPage } from '@/pages/category/CategoryOverviewPage';
import { JobsPage } from '@/pages/category/JobsPage';
import { JobPage } from '@/pages/category/JobPage';
import { type MappingLoaderData, MappingPage } from '@/pages/category/MappingPage';
import { type ActionLoaderData, ActionDetailPage } from '@/pages/category/ActionDetailPage';
import { NewMappingPage } from '@/pages/category/NewMappingPage';

/**
 * Creates the application's routing configuration.
 */
export const router = createBrowserRouter([
    {
        path: routes.home.path,
        id: routes.home.id,
        Component: RootLayout,
        ErrorBoundary: ErrorPage,
        children: [
            {
                index: true,
                Component: Home,
            },
            {
                path: routes.datasources,
                handle: { breadcrumb: 'Datasources' },
                children: [
                    {
                        index: true,
                        loader: DatasourcesPage.loader,
                        Component: DatasourcesPage,
                    },
                    {
                        path: ':id',
                        loader: DatasourceDetailPage.loader,
                        Component: DatasourceDetailPage,
                        handle: {
                            breadcrumb: (data: DatasourceLoaderData) => data.datasource.label,
                        },
                    },
                ],
            },
            // Work of other colleague, left here for future merge
            // {
            //     path: routes.adminer,
            //     loader: AdminerPage.loader,
            //     Component: AdminerPage,
            //     handle: { breadcrumb: 'Adminer' },
            // },
            {
                path: routes.categories,
                Component: SchemaCategoriesPage,
                handle: { breadcrumb: 'Schema Categories' },
                children: [
                    {
                        index: true,
                        Component: CategoriesPage,
                        loader: CategoriesPage.loader,
                    },
                    {
                        path: ':categoryId',
                        loader: CategoryPage.loader,
                        Component: CategoryPage,
                        handle: {
                            breadcrumb: (data: CategoryLoaderData) => data.category.label,
                        },
                        children: [
                            {
                                index: true,
                                id: routes.category.index.id,
                                loader: CategoryOverviewPage.loader,
                                Component: CategoryOverviewPage,
                            },
                            {
                                id: routes.category.editor.id,
                                path: routes.category.editor.path,
                                loader: CategoryEditorPage.loader,
                                Component: CategoryEditorPage,
                                handle: { breadcrumb: 'Editor' },
                            },
                            {
                                id: routes.category.datasources.id,
                                path: routes.category.datasources.path,
                                handle: { breadcrumb: 'Datasources' },
                                children: [
                                    {
                                        index: true,
                                        loader: DatasourcesInCategoryPage.loader,
                                        Component: DatasourcesInCategoryPage,
                                    },
                                    {
                                        path: ':id',
                                        loader: DatasourceInCategoryPage.loader,
                                        Component: DatasourceInCategoryPage,
                                        handle: {
                                            breadcrumb: (data: DatasourceLoaderData) => data.datasource.label,
                                        },
                                    },
                                ],
                            },
                            {
                                id: routes.category.newMapping.id,
                                path: routes.category.newMapping.path,
                                loader: NewMappingPage.loader,
                                Component: NewMappingPage,
                                handle: { breadcrumb: 'New Mapping' },
                            },
                            {
                                id: routes.category.mapping.id,
                                path: routes.category.mapping.path,
                                loader: MappingPage.loader,
                                Component: MappingPage,
                                handle: {
                                    breadcrumb: (data: MappingLoaderData) => data.mapping.kindName,
                                },
                            },
                            {
                                id: routes.category.actions.id,
                                path: routes.category.actions.path,
                                handle: { breadcrumb: 'Actions' },
                                children: [
                                    {
                                        index: true,
                                        loader: ActionsPage.loader,
                                        Component: ActionsPage,
                                    },
                                    {
                                        id: 'add-action',
                                        path: 'add',
                                        Component: AddActionPage,
                                        handle: { breadcrumb: 'Add' },
                                    },
                                    {
                                        id: 'action',
                                        path: ':actionId',
                                        loader: ActionDetailPage.loader,
                                        Component: ActionDetailPage,
                                        handle: {
                                            breadcrumb: (data: ActionLoaderData) => data.action.label,
                                        },
                                    },
                                ],
                            },
                            {
                                id: routes.category.jobs.id,
                                path: routes.category.jobs.path,
                                handle: { breadcrumb: 'Jobs' },
                                children: [
                                    {
                                        index: true,
                                        Component: JobsPage,
                                    },
                                    {
                                        path: ':jobId',
                                        loader: JobPage.loader,
                                        Component: JobPage,
                                        handle: { breadcrumb: 'Job Details' },
                                    },
                                ],
                            },
                        ],
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
], {
    future: {
        v7_relativeSplatPath: true,
        v7_fetcherPersist: true,
        v7_normalizeFormMethod: true,
        v7_partialHydration: true,
        v7_skipActionErrorRevalidation: true,
    },
});
