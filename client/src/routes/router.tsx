import { createBrowserRouter } from 'react-router-dom';
import { Home } from '@/pages/Home';
import { SchemaCategory, schemaCategoryLoader } from '@/pages/project/SchemaCategory';
import { About } from '@/pages/About';
import { routes } from '@/routes/routes';
import { ErrorPage } from '@/pages/errorPages';
import { ProjectIndex, projectIndexLoader } from '@/pages/ProjectIndex';
import { DatasourcesPage } from '@/pages/DatasourcesPage';
import { DatasourceDetailPage } from '@/pages/DatasourceDetailPage';
import { AdminerPage } from '@/pages/AdminerPage';

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
        path: routes.project.index.path,
        Component: ProjectIndex,
        loader: projectIndexLoader,
        ErrorBoundary: ErrorPage,
        children: [
            {
                index: true,
                id: routes.project.index.id,
                loader: schemaCategoryLoader,
                Component: SchemaCategory,
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
]);
