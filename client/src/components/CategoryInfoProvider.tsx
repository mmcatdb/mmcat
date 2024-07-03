import { type SchemaCategoryInfo } from '@/types/schema';
import { createContext, useContext, useState } from 'react';

type CategoryInfoContext = {
    category: SchemaCategoryInfo;
    setCategory: (category: SchemaCategoryInfo) => void;
};

export const CategoryInfoContext = createContext<CategoryInfoContext | undefined>(undefined);

type CategoryInfoProviderProps = Readonly<{
    children: React.ReactNode;
    category: SchemaCategoryInfo;
}>;

export function CategoryInfoProvider({ children, category: inputCategory }: CategoryInfoProviderProps) {
    console.log({ inputCategory });

    const [ category, setCategory ] = useState(inputCategory);

    return (
        <CategoryInfoContext.Provider value={{ category, setCategory }}>
            {children}
        </CategoryInfoContext.Provider>
    );
}

export function useCategoryInfo(): CategoryInfoContext {
    const context = useContext(CategoryInfoContext);
    if (context === undefined)
        throw new Error('useCategoryInfo must be used within an CategoryInfoProvider');

    return context;
}
