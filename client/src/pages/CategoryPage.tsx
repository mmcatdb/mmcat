import { Outlet, type Params, useLoaderData } from 'react-router-dom';
import { api } from '@/api';
import { CategoryInfo } from '@/types/schema';
import { CategoryInfoProvider } from '@/components/context/CategoryInfoProvider';

/**
 * Main page for the schema category project.
 */
export function CategoryPage() {
    const { category } = useLoaderData() as CategoryLoaderData;

    return (
        <CategoryInfoProvider category={category}>
            {/* This is left here for testing purposes only. The functionality should be moved to Backend in the future. */}
            {/* <div className='z-20 fixed bottom-16 left-0 right-0 h-0 flex justify-center'>
                <SessionSelect />
            </div> */}

            <Outlet />
        </CategoryInfoProvider>
    );
}

export type CategoryLoaderData = {
    category: CategoryInfo;
};

CategoryPage.loader = async ({ params: { categoryId } }: { params: Params<'categoryId'> }) => {
    if (!categoryId)
        throw new Error('Category ID is required');

    return {
        category: await api.schemas.getCategoryInfo({ id: categoryId }).then(response => {
            if (!response.status)
                throw new Error('Failed to load category info');

            return CategoryInfo.fromResponse(response.data);
        }),
    };
};
