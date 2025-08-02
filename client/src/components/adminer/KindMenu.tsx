import { type Dispatch, useCallback, useMemo } from 'react';
import { Spinner, Select, SelectItem } from '@heroui/react';
import { useFetchData } from '@/components/adminer/useFetchData';
import { api } from '@/api';
import type { AdminerFilterQueryStateAction } from '@/components/adminer/adminerReducer';
import type { Id } from '@/types/id';

export const UNLABELED = '#unlabeled';

type KindMenuProps = {
    /** The id of the current datasource. */
    datasourceId: Id;
    /** Name of selected kind. */
    kind: string | undefined;
    /** If 'true', additional kind name for unlabeled nodes from Neo4j is added. */
    showUnlabeled: boolean;
    /** A function for state updating. */
    dispatch: Dispatch<AdminerFilterQueryStateAction>;
};

type Option = {
    value: string;
    label: string;
};

/**
 * Component for selecting kind
 */
export function KindMenu({ datasourceId, kind, showUnlabeled, dispatch }: KindMenuProps) {
    const fetchFunction = useCallback(() => api.adminer.getKindNames({ datasourceId }), [ datasourceId ]);

    const { fetchedData, loading, error } = useFetchData(fetchFunction);

    const selectItems = useMemo(() => {
        const items: Option[] = [];

        if (fetchedData && fetchedData.length > 0) {
            fetchedData.forEach(name => (
                items.push({ label: name, value: name })
            ));

            if (showUnlabeled)
                items.push({ label: UNLABELED, value: UNLABELED });
        }

        return items;
    }, [ fetchedData, showUnlabeled ]);

    if (error)
        return <p className='ml-1 mt-1'>{error}</p>;

    if (loading) {
        return (
            <div className='h-10 flex items-center justify-center'>
                <Spinner />
            </div>
        );
    }

    if (!fetchedData || fetchedData.length === 0)
        return <span>No kinds to display.</span>;

    return (
        <Select
            items={selectItems}
            aria-label='Kind'
            labelPlacement='outside-left'
            classNames={
                { label: 'sr-only' }
            }
            size='sm'
            placeholder='Select kind'
            className='max-w-xs px-0'
            selectedKeys={kind ? [ kind ] : undefined}
        >
            {item => (
                <SelectItem
                    key={item.value}
                    onPress={() => dispatch({ type: 'kind', newKind: item.value })}
                >
                    {item.label}
                </SelectItem>
            )}
        </Select>
    );
}
