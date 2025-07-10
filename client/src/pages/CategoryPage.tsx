import { Outlet, type Params, useLoaderData } from 'react-router-dom';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { CategoryInfoProvider } from '@/components/CategoryInfoProvider';
import { SessionSelect } from '@/components/SessionSelect';

/**
 * Main page for the schema category project.
 */
export function CategoryPage() {
    const { category } = useLoaderData() as CategoryLoaderData;

    return (
        <CategoryInfoProvider category={category}>
            {/* This is left here for testing purposes only. The functionality should be moved to Backend in the future. */}
            <div className='z-20 fixed bottom-12 left-0 right-0 h-0 flex justify-center'>
                <SessionSelect />
            </div>

            <Outlet />
        </CategoryInfoProvider>
    );
}

CategoryPage.loader = categoryLoader;

export type CategoryLoaderData = {
    category: SchemaCategoryInfo;
};

async function categoryLoader({ params: { categoryId } }: { params: Params<'categoryId'> }) {
    if (!categoryId)
        throw new Error('Category ID is required');

    return {
        category: await api.schemas.getCategoryInfo({ id: categoryId }).then(response => {
            if (!response.status)
                throw new Error('Failed to load category info');

            return SchemaCategoryInfo.fromResponse(response.data);
        }),
    };
}
