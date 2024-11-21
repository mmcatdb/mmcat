import { createBrowserRouter, useLoaderData, type Params } from 'react-router-dom';
import { Home } from '@/pages/Home';
import { SchemaCategory, schemaCategoryLoader } from '@/pages/category/SchemaCategory';
import { About } from '@/pages/About';
import { routes } from '@/routes/routes';
import { ErrorPage } from '@/pages/errorPages';
import { CategoryIndex, categoryIndexLoader, type CategoryIndexLoaderData } from '@/pages/CategoryIndex';
import { DatasourcesPage, DatasourcesPageOverview } from '@/pages/DatasourcesPage';
import { DatasourceDetailPage } from '@/pages/DatasourceDetailPage';
import { AdminerPage } from '@/pages/AdminerPage';
import { SchemaCategoriesPage } from '@/pages/SchemaCategoriesPage';
import { QueryingPage } from '@/pages/QueryingPage';
import { RootLayout } from '@/components/RootLayout';
import { Mapping } from '@/types/mapping';
import { api } from '@/api';
import { useCategoryInfo } from '@/components/CategoryInfoProvider';
import { DatasourcesInCategoryPage } from '@/pages/category/DatasourcesInCategory';

type MappingLoaderData = {
    mapping: Mapping;
};

async function mappingLoader({ params: { mappingId } }: { params: Params<'mappingId'> }) {
    if (!mappingId)
        throw new Error('Mapping ID is required');

    return {
        mapping: await api.mappings.getMapping({ id: mappingId }).then(response => {
            if (!response.status)
                throw new Error('Failed to load mapping');

            return Mapping.fromServer(response.data);
        }),
    };
}

function MappingDisplay() {
    const { mapping } = useLoaderData() as MappingLoaderData;
    const { category } = useCategoryInfo();

    return (
        <div className='p-4 bg-slate-500'>
            <h1>Mapping {mapping.kindName}</h1>
            <p>
                Some text.
            </p>
            <p>
                category 1: {category.label}
            </p>
        </div>
    );
}

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
                handle: { breadcrumb: (data: CategoryIndexLoaderData) => data.category.label },
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
                        id: routes.category.querying.id,
                        path: routes.category.querying.path,
                        Component: QueryingPage,
                        handle: { breadcrumb: 'Querying' },
                    },
                    {
                        id: routes.category.datasources.id,
                        path: routes.category.datasources.path,
                        Component: DatasourcesInCategoryPage,
                        handle: { breadcrumb: 'Datasources' },
                        children: [
                            {
                                id: 'mapping',
                                path: 'mappings/:mappingId',
                                loader: mappingLoader,
                                Component: MappingDisplay,
                                handle: { breadcrumb: (data: MappingLoaderData) => data.mapping.kindName },
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
]);
