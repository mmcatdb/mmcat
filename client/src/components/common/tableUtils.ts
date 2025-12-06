import { type SortDescriptor } from '@heroui/react';
import { useState, useMemo } from 'react';

export const REMOVED_SORT_KEY = Symbol('removed-sort-key');

/** If the value for a column is `REMOVED_SORT_KEY`, the current sort descriptor should be reset to its default column (if it uses this column). */
export type Comparator<TItem> = Record<string, ItemComparator<TItem> | undefined | typeof REMOVED_SORT_KEY>;
type ItemComparator<TItem> = (a: TItem, b: TItem) => number;

/**
 * Hook to manage sorting of items for HeroUI tables.
 * Using SortDescriptor for HeroUI's 2.0 Table `onSortChange()`.
 */
export function useSortable<TItem>(items: TItem[], initialSortDescriptor: SortDescriptor, comparator?: Comparator<TItem>) {
    const [ sortDescriptor, setSortDescriptor ] = useState(initialSortDescriptor);

    let descriptor = sortDescriptor;
    if (comparator?.[descriptor.column] === REMOVED_SORT_KEY && descriptor.column !== initialSortDescriptor.column) {
        // Reset to default sort column if the comparator for the current column is null.
        // However, do it only once to avoid infinite loops.
        setSortDescriptor(initialSortDescriptor);
        descriptor = initialSortDescriptor;
    }

    const { column, direction } = descriptor;

    const sorted = useMemo(() => {
        const fieldComparator = getItemComparator(column as string, direction, comparator);
        return items.toSorted(fieldComparator);
    }, [ items, column, direction, comparator ]);

    return { sorted, sortDescriptor, setSortDescriptor };
}

type SortDirection = SortDescriptor['direction'];

const DEFAULT_SORT_DIRECTION = 'ascending' as const satisfies SortDirection;

function getItemComparator<TItem>(key: string, direction: SortDirection, comparator: Comparator<TItem> | undefined): ItemComparator<TItem> {
    const customComparator = comparator?.[key];
    if (customComparator) {
        if (customComparator !== REMOVED_SORT_KEY) {
            return direction === DEFAULT_SORT_DIRECTION
                ? (a: TItem, b: TItem) => customComparator(a, b)
                : (a: TItem, b: TItem) => -customComparator(a, b);
        }

        console.warn(`Comparator for key "${key}" is REMOVED_SORT_KEY, falling back to default comparator.`);
    }

    return direction === DEFAULT_SORT_DIRECTION
        ? (a: TItem, b: TItem) => defaultComparator(a, b, key)
        : (a: TItem, b: TItem) => -defaultComparator(a, b, key);
}

function defaultComparator(a: any, b: any, key: string): number {
    if (!(key in a) || !(key in b))
        console.warn(`Default comparator: key "${key}" not found in one of the items.`);

    const aValue = a[key];
    const bValue = b[key];

    if (typeof aValue === 'string' && typeof bValue === 'string')
        return defaultStringComparator(aValue, bValue);

    if (aValue < bValue)
        return -1;
    else if (aValue > bValue)
        return 1;
    else
        return 0;
}

function defaultStringComparator(a: string, b: string): number {
    const aLower = a.toLowerCase();
    const bLower = b.toLowerCase();

    // Or this?
    // return aLower.localeCompare(bLower);

    if (aLower < bLower)
        return -1;
    else if (aLower > bLower)
        return 1;
    else
        return 0;
}

type PaginationResult<TItem> = {
    paginated: TItem[];
    paginator: Paginator | undefined;
};

export type Paginator = {
    page: number;
    total: number;
    onChange: (newPage: number) => void;
};

export function usePagination<TItem>(items: TItem[], itemsPerPage: number | undefined): PaginationResult<TItem> {
    const [ innerPage, setInnerPage ] = useState(1);

    return useMemo(() => {
        if (!itemsPerPage || items.length <= itemsPerPage) {
            return {
                paginated: items,
                paginator: undefined,
            };
        }

        const total = Math.max(1, Math.ceil(items.length / itemsPerPage));
        const page = Math.min(Math.max(1, innerPage), total);

        const start = (page - 1) * itemsPerPage;
        const end = start + itemsPerPage;

        return {
            paginated: items.slice(start, end),
            paginator: {
                page,
                total,
                onChange: setInnerPage,
            },
        };
    }, [ items, itemsPerPage, innerPage ]);
}
