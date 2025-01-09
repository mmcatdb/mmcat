import { useCallback } from 'react';
import { Spinner, Select, SelectItem } from '@nextui-org/react';
import { useFetchData } from '@/components/adminer/useFetchData';
import { api } from '@/api';
import type { AdminerStateAction } from '@/types/adminer/Reducer';
import type { Id } from '@/types/id';

type KindMenuProps = Readonly<{
    datasourceId: Id;
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
                selectedKeys={ kind ? [ kind ] : undefined }
            >
                {fetchedData.data.map((name, index) => (
                    <SelectItem
                        key={name}
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
