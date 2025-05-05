import { Outlet, type Params, useLoaderData } from 'react-router-dom';
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
