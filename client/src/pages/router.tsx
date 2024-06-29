import { createBrowserRouter } from 'react-router-dom';
import { Home } from '@/pages/Home';
import { SchemaCategory, schemaCategoryLoader } from '@/pages/project/SchemaCategory';
import { About } from '@/pages/About';
import { routes } from './routes';
import { ErrorPage } from './errorPages';
import { ProjectIndex, projectIndexLoader } from './ProjectIndex';
import { Databases } from './project/Databases';

export const router = createBrowserRouter([
    {
        id: routes.home.id,
        path: routes.home.path,
        Component: Home,
        ErrorBoundary: ErrorPage,
    },
    {
        path: routes.about,
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
            {
                id: routes.project.databases.id,
                path: routes.project.databases.path,
                Component: Databases,
            },
        ],
    },
]);
