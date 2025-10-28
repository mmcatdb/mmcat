import { createBrowserRouter } from 'react-router-dom';
import { routes } from '@/routes/routes';
import { RootLayout } from '@/components/RootLayout';
import { HomePage } from '@/pages/HomePage';
import { CategoryEditorPage } from '@/pages/category/CategoryEditorPage';
import { ErrorPage } from '@/pages/errorPages';
import { CategoryPage, type CategoryLoaderData } from '@/pages/CategoryPage';
import { DatasourcesPage } from '@/pages/DatasourcesPage';
import { type DatasourceLoaderData, DatasourceDetailPage, DatasourceInCategoryPage } from '@/pages/DatasourcePage';
import { AdminerPage } from '@/pages/AdminerPage';
import { CategoriesPage } from '@/pages/CategoriesPage';
import { DatasourcesInCategoryPage } from '@/pages/category/DatasourcesInCategoryPage';
import { ActionsPage } from '@/pages/category/ActionsPage';
import { NewActionPage } from '@/pages/category/NewActionPage';
import { CategoryOverviewPage } from '@/pages/category/CategoryOverviewPage';
import { JobsPage } from '@/pages/category/JobsPage';
import { JobPage } from '@/pages/category/JobPage';
import { type MappingLoaderData, MappingPage } from '@/pages/category/MappingPage';
import { type ActionLoaderData, ActionDetailPage } from '@/pages/category/ActionDetailPage';
import { NewMappingPage } from '@/pages/category/NewMappingPage';
import { NewQueryPage } from '@/pages/category/NewQueryPage';
import { QueriesPage } from '@/pages/category/QueriesPage';
import { type QueryLoaderData, QueryPage } from '@/pages/category/QueryPage';

/**
 * Creates the application's routing configuration.
 */
export const router = createBrowserRouter([ {
    id: routes.home.id,
    path: routes.home.path,
    Component: RootLayout,
    ErrorBoundary: ErrorPage,
    children: [ {
        index: true,
        loader: HomePage.loader,
        Component: HomePage,
    }, {
        path: routes.datasources.list.path,
        handle: { breadcrumb: 'Datasources' },
        children: [ {
            index: true,
            loader: DatasourcesPage.loader,
            Component: DatasourcesPage,
        }, {
            path: routes.datasources.detail.path,
            loader: DatasourceDetailPage.loader,
            Component: DatasourceDetailPage,
            handle: { breadcrumb: (data: DatasourceLoaderData) => data.datasource.label },
        } ],
    }, {
        path: routes.adminer,
        loader: AdminerPage.loader,
        Component: AdminerPage,
        handle: { breadcrumb: 'Adminer' },
    }, {
        path: routes.categories,
        handle: { breadcrumb: 'Schema Categories' },
        children: [ {
            index: true,
            Component: CategoriesPage,
            loader: CategoriesPage.loader,
        }, {
            path: routes.category.index.path,
            loader: CategoryPage.loader,
            Component: CategoryPage,
            handle: { breadcrumb: (data: CategoryLoaderData) => data.category.label },
            children: [ {
                index: true,
                id: routes.category.index.id,
                loader: CategoryOverviewPage.loader,
                Component: CategoryOverviewPage,
            }, {
                id: routes.category.editor.id,
                path: routes.category.editor.path,
                loader: CategoryEditorPage.loader,
                Component: CategoryEditorPage,
                handle: { breadcrumb: 'Editor' },
            }, {
                id: routes.category.datasources.list.id,
                path: routes.category.datasources.list.path,
                handle: { breadcrumb: 'Datasources' },
                children: [ {
                    index: true,
                    loader: DatasourcesInCategoryPage.loader,
                    Component: DatasourcesInCategoryPage,
                }, {
                    path: routes.category.datasources.detail.path,
                    loader: DatasourceInCategoryPage.loader,
                    Component: DatasourceInCategoryPage,
                    handle: { breadcrumb: (data: DatasourceLoaderData) => data.datasource.label },
                }, {
                    id: routes.category.datasources.newMapping.id,
                    path: routes.category.datasources.newMapping.path,
                    loader: NewMappingPage.loader,
                    Component: NewMappingPage,
                    // TODO
                    handle: { breadcrumb: 'New Mapping' },
                } ],
            }, {
                id: routes.category.mapping.id,
                path: routes.category.mapping.path,
                loader: MappingPage.loader,
                Component: MappingPage,
                handle: { breadcrumb: (data: MappingLoaderData) => data.mapping.kindName },
            }, {
                id: routes.category.actions.list.id,
                path: routes.category.actions.list.path,
                handle: { breadcrumb: 'Actions' },
                children: [ {
                    index: true,
                    loader: ActionsPage.loader,
                    Component: ActionsPage,
                }, {
                    id: routes.category.actions.detail.id,
                    path: routes.category.actions.detail.path,
                    loader: ActionDetailPage.loader,
                    Component: ActionDetailPage,
                    handle: { breadcrumb: (data: ActionLoaderData) => data.action.label },
                }, {
                    id: routes.category.actions.new.id,
                    path: routes.category.actions.new.path,
                    Component: NewActionPage,
                    handle: { breadcrumb: 'New' },
                } ],
            }, {
                id: routes.category.jobs.id,
                path: routes.category.jobs.path,
                handle: { breadcrumb: 'Jobs' },
                children: [ {
                    index: true,
                    Component: JobsPage,
                }, {
                    path: routes.category.job.path,
                    Component: JobPage,
                    handle: { breadcrumb: 'Job Details' },
                } ],
            }, {
                id: routes.category.queries.list.id,
                path: routes.category.queries.list.path,
                handle: { breadcrumb: 'Querying' },
                children: [ {
                    index: true,
                    loader: QueriesPage.loader,
                    Component: QueriesPage,
                }, {
                    id: routes.category.queries.detail.id,
                    path: routes.category.queries.detail.path,
                    loader: QueryPage.loader,
                    Component: QueryPage,
                    handle: { breadcrumb: (data: QueryLoaderData) => data.query.label },
                }, {
                    id: routes.category.queries.new.id,
                    path: routes.category.queries.new.path,
                    Component: NewQueryPage,
                    loader: NewQueryPage.loader,
                    handle: { breadcrumb: 'New' },
                } ],
            } ],
        } ],
    } ],
}, {
    // Catch-all route for 404 errors
    path: '*',
    loader: () => {
        throw new Response('Not Found', { status: 404, statusText: 'Not Found' });
    },
    element: <ErrorPage />,
    ErrorBoundary: ErrorPage,
} ], {
    future: {
        v7_relativeSplatPath: true,
        v7_fetcherPersist: true,
        v7_normalizeFormMethod: true,
        v7_partialHydration: true,
        v7_skipActionErrorRevalidation: true,
    },
});
