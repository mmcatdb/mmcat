import { Suspense, useMemo } from 'react';
import { Outlet, type Params, useLoaderData, useMatches, defer, Await } from 'react-router-dom';
import { usePreferences } from '@/components/PreferencesProvider';
import { Link, portals } from '@/components/common';
import { ThemeToggle } from '@/components/CommonPage';
import clsx from 'clsx';
import { routes } from './routes';
import { CollapseContextToggle } from '@/components/project/context';
import { api, type Resolved } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { LoadingPage } from './errorPages';
import { CategoryInfoProvider, useCategoryInfo } from '@/components/CategoryInfoProvider';

export function ProjectIndex() {
    const loaderData = useLoaderData() as ProjectIndexLoaderData;

    return (
        <Suspense fallback={<LoadingPage />}>
            <Await resolve={loaderData.category}>
                {(category: Resolved<ProjectIndexLoaderData, 'category'>) => (
                    <CategoryInfoProvider category={category}>
                        <ProjectIndexInner />
                    </CategoryInfoProvider>
                )}
            </Await>
        </Suspense>
    );
}

type ProjectIndexLoaderData = {
    category: Promise<SchemaCategoryInfo>;
};

export function projectIndexLoader({ params: { projectId } }: { params: Params<'projectId'> }) {
    if (!projectId)
        throw new Error('Project ID is required');

    return defer({
        category: api.schemas.getCategoryInfo({ id: projectId }).then(response => {
            if (!response.status)
                throw new Error('Failed to load project info');

            return SchemaCategoryInfo.fromServer(response.data);
        }),
    } satisfies ProjectIndexLoaderData);
}

function ProjectIndexInner() {
    const { theme, isCollapsed } = usePreferences().preferences;
    const { category } = useCategoryInfo();

    return (
        <div className={clsx('mm-layout text-foreground bg-background', theme, isCollapsed && 'collapsed')}>
            <ProjectMenu projectId={category.id} />
            <div className='mm-context'>
                <div className='mm-context-header flex items-center px-2'>
                    {category.label}
                </div>
                <div className='mm-context-content main-scroller main-scroller-no-center' id={portals.context} />
            </div>
            <div className='mm-content main-scroller main-scroller-no-center p-4'>
                <Outlet />
            </div>
        </div>
    );
}

function ProjectMenu({ projectId }: Readonly<{ projectId: string }>) {
    const menuItems = useMemo(() => createMenuItems(projectId), [ projectId ]);
    const matches = useMatches();

    return (
        <div className='mm-menu'>
            <nav>
                {menuItems.map((item) => {
                    const isMatched = matches.find(match => match.id === item.id) !== undefined;

                    return (
                        <Link key={item.id} to={item.path} isDisabled={isMatched} className='mm-menu-element flex items-center justify-center'>
                            {item.label}
                        </Link>
                    );
                })}
            </nav>
            <div className='mm-menu-element flex items-center justify-center'>
                <CollapseContextToggle />
            </div>
            <div className='mm-menu-element flex items-center justify-center'>
                <ThemeToggle />
            </div>
        </div>
    );
}

type MenuItem = {
    label: string;
    icon: string;
    id: string;
    path: string;
};

function createMenuItems(projectId: string): MenuItem[] {
    return [
        {
            label: 'Home',
            icon: 'home',
            id: routes.home.id,
            path: routes.home.path,
        },
        {
            label: 'Project',
            icon: 'project',
            id: routes.project.index.id,
            path: routes.project.index.resolve({ projectId }),
        },
        {
            label: 'Databases',
            icon: 'databases',
            id: routes.project.databases.id,
            path: routes.project.databases.resolve({ projectId }),
        },
    ];
}
