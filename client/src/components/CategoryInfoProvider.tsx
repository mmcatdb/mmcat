import { type SchemaCategoryInfo } from '@/types/schema';
import { createContext, type ReactNode, useContext, useState } from 'react';

type CategoryInfoContext = {
    category: SchemaCategoryInfo;
    setCategory: (category: SchemaCategoryInfo) => void;
};

/**
 * Context for providing category information to child components.
 */
export const CategoryInfoContext = createContext<CategoryInfoContext | undefined>(undefined);

type CategoryInfoProviderProps = {
    children: ReactNode;
    category: SchemaCategoryInfo;
};

/**
 * Provides category information to child components via context.
 */
export function CategoryInfoProvider({ children, category: inputCategory }: CategoryInfoProviderProps) {
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
