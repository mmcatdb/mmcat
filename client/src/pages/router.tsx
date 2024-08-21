import { createBrowserRouter } from 'react-router-dom';
import { Home } from '@/pages/Home';
import { SchemaCategory, schemaCategoryLoader } from '@/pages/project/SchemaCategory';
import { About } from '@/pages/About';
import { routes } from '@/pages/routes';
import { ErrorPage } from '@/pages/errorPages';
import { ProjectIndex, projectIndexLoader } from '@/pages/ProjectIndex';
import { DatabasesPage } from '@/pages/DatabasesPage';

export const router = createBrowserRouter([
    {
        id: routes.home.id,
        path: routes.home.path,
        Component: Home,
        ErrorBoundary: ErrorPage,
    },
    {
        path: 'about', // TODO: same problem as in the routes.ts
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
        path: 'databases',
        Component: DatabasesPage,
    },
]);
