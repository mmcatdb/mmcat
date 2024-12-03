import { useCallback } from 'react';
import { Spinner, Select, SelectItem } from '@nextui-org/react';
import { useFetchData } from '@/components/adminer/useFetchData';
import type { AdminerStateAction } from '@/types/adminer/Reducer';
import { api } from '@/api';

type KindMenuProps = Readonly<{
    datasourceId: string;
    kind: string | undefined;
    showUnlabeled: boolean;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

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

    return (
        fetchedData && fetchedData.data.length > 0 ? (
            <Select
                label='Kind'
                placeholder='Select kind'
                className='max-w-xs'
                defaultSelectedKeys={ [ kind && fetchedData.data.includes(kind) ? fetchedData.data.findIndex(name => name === kind).toString() : kind ? kind : '' ] }
            >
                {fetchedData.data.map((name, index) => (
                    <SelectItem
                        key={index}
                        onPress={() => dispatch({ type: 'kind', newKind: name })}
                    >
                        {name}
                    </SelectItem>
                ))}

                {showUnlabeled && (<SelectItem
                    key='unlabeled'
                    onPress={() => dispatch({ type: 'kind', newKind: 'unlabeled' })}
                >
                    Unlabeled
                </SelectItem>
                )}

            </Select>
        ) : (
            <span>No kinds to display.</span>
        )
    );
}
