import { Outlet, type Params, useLoaderData } from 'react-router-dom';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { CategoryInfoProvider } from '@/components/CategoryInfoProvider';
import { SessionSelect } from '@/components/SessionSelect';

export function CategoryIndex() {
    const { category } = useLoaderData() as CategoryIndexLoaderData;

    return (
        <CategoryInfoProvider category={category}>
            <div className='z-20 fixed top-0 left-0 right-0 h-0 flex justify-center'>
                <SessionSelect />
            </div>

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
    return (
        <Outlet />
    );
}
