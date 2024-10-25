import { createBrowserRouter } from 'react-router-dom';
import { Home } from '@/pages/Home';
import { SchemaCategory, schemaCategoryLoader } from '@/pages/category/SchemaCategory';
import { About } from '@/pages/About';
import { routes } from '@/routes/routes';
import { ErrorPage } from '@/pages/errorPages';
import { ProjectIndex, projectIndexLoader } from '@/pages/CategoryIndex';
import { DatasourcesPage } from '@/pages/DatasourcesPage';
import { DatasourceDetailPage } from '@/pages/DatasourceDetailPage';
import { AdminerPage } from '@/pages/AdminerPage';
import { SchemaCategoriesPage } from '@/pages/SchemaCategoriesPage';
import { ModelsPage } from '@/pages/ModelsPage';
import { QueryingPage } from '@/pages/QueryingPage';

export const router = createBrowserRouter([
    {
        id: routes.home.id,
        path: routes.home.path,
        Component: Home,
        ErrorBoundary: ErrorPage,
    },
    {
        path: 'about',
        Component: About,
    },
    {
        path: routes.category.index.path,
        Component: ProjectIndex,
        loader: projectIndexLoader,
        ErrorBoundary: ErrorPage,
        children: [
            {
                index: true,
                id: routes.category.index.id,
                loader: schemaCategoryLoader,
                Component: SchemaCategory,
            },
            {
                path: routes.category.models.path,
                Component: ModelsPage,
            },
            {
                path: routes.category.querying.path,
                Component: QueryingPage,
            },
        ],
    },
    {
        path: 'datasources',
        Component: DatasourcesPage,
    },
    {
        path: 'datasources/:id',
        Component: DatasourceDetailPage,
    },
    {
        path: 'adminer',
        Component: AdminerPage,
    },
    {
        path: 'schema-categories',
        Component: SchemaCategoriesPage,
    },
]);
