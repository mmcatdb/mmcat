import { useCallback } from 'react';
import { Spinner, Select, SelectItem } from '@nextui-org/react';
import { useFetchData } from '@/components/adminer/useFetchData';
import { api } from '@/api';
import type { AdminerFilterQueryStateAction } from '@/components/adminer/filterQueryReducer';
import type { Id } from '@/types/id';

export const UNLABELED = '#unlabeled';

type KindMenuProps = Readonly<{
    datasourceId: Id;
    kind: string | undefined;
    showUnlabeled: boolean;
    dispatch: React.Dispatch<AdminerFilterQueryStateAction>;
}>;

type KindLabelValues = {label: string, value: string }[];

export function KindMenu({ datasourceId, kind, showUnlabeled, dispatch }: KindMenuProps) {
    const fetchFunction = useCallback(() => {
        return api.adminer.getKindNames({ datasourceId });
    }, [ datasourceId ]);
    const { fetchedData, loading, error } = useFetchData(fetchFunction);

    if (loading) {
        return (
            <div className='h-10 flex items-center justify-center'>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;

    const selectItems: KindLabelValues = [];
    if (fetchedData && fetchedData.data.length > 0) {
        fetchedData.data.forEach(name => (
            selectItems.push({ label: name, value: name })
        ));

        if (showUnlabeled)
            selectItems.push({ label: UNLABELED, value: UNLABELED });
    }

    return (
        fetchedData && fetchedData.data.length > 0 ? (
            <Select
                items={selectItems}
                aria-label='Kind'
                labelPlacement='outside-left'
                classNames={
                    { label:'sr-only' }
                }
                size='sm'
                placeholder='Select kind'
                className='max-w-xs px-0'
                selectedKeys={ kind ? [ kind ] : undefined }
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
        ) : (
            <span>No kinds to display.</span>
        )
    );
}
