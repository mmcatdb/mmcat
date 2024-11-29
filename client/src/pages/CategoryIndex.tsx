import { useMemo } from 'react';
import { Outlet, type Params, useLoaderData, useMatches } from 'react-router-dom';
import { CustomLink } from '@/components/common';
import { ThemeToggle } from '@/components/RootLayout';
import { routes } from '../routes/routes';
import { CollapseContextToggle } from '@/components/project/context';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { CategoryInfoProvider, useCategoryInfo } from '@/components/CategoryInfoProvider';

export function CategoryIndex() {
    const { category } = useLoaderData() as CategoryIndexLoaderData;

    return (
        <CategoryInfoProvider category={category}>
            <CategoryIndexInner />
        </CategoryInfoProvider>
    );
}

export type CategoryIndexLoaderData = {
    category: SchemaCategoryInfo;
};

export async function categoryIndexLoader({ params: { categoryId } }: { params: Params<'categoryId'> }) {
    if (!categoryId)
        throw new Error('Category ID is required');

    return {
        category: await api.schemas.getCategoryInfo({ id: categoryId }).then(response => {
            if (!response.status)
                throw new Error('Failed to load category info');

            return SchemaCategoryInfo.fromServer(response.data);
        }),
    };
}

function CategoryIndexInner() {
    const { category } = useCategoryInfo();

    return (
        <div>
            <h1 className='text-xl'>
                {category.label}, sv.{category.systemVersionId}
            </h1>
            <Outlet />
        </div>
    );
}

function CategoryMenu({ categoryId }: Readonly<{ categoryId: string }>) {
    const menuItems = useMemo(() => createMenuItems(categoryId), [ categoryId ]);
    const matches = useMatches();

    return (
        <div className='mm-menu'>
            <nav>
                {menuItems.map((item) => {
                    const isMatched = matches.find(match => match.id === item.id) !== undefined;

                    return (
                        <CustomLink key={item.id} to={item.path} isDisabled={isMatched} className='mm-menu-element flex items-center justify-center'>
                            {item.label}
                        </CustomLink>
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

function createMenuItems(categoryId: string): MenuItem[] {
    return [
        {
            label: 'Home',
            icon: 'home',
            id: routes.home.id,
            path: routes.home.path,
        },
        {
            label: 'Category',
            icon: 'category',
            id: routes.category.index.id,
            path: routes.category.index.resolve({ categoryId }),
        },
        {
            label: 'Datasources',
            icon: 'datasources',
            id: routes.category.datasources.id,
            path: routes.category.datasources.resolve({ categoryId }),
        },
    ];
}
