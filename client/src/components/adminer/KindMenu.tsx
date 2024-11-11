import { useMemo } from 'react';
import { useFetchData } from '@/components/adminer/useFetchData';
import { type AdminerStateAction } from '@/types/adminer/Reducer';
import { Spinner, Select, SelectItem } from '@nextui-org/react';

type KindMenuProps = Readonly<{
    datasourceId: string;
    showUnlabeled: boolean;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function KindMenu({ datasourceId, showUnlabeled, dispatch }: KindMenuProps) {
    const urlParams = useMemo(() => {
        return { datasourceId: datasourceId };
    }, [ datasourceId ]);

    const { fetchedData, loading, error } = useFetchData(urlParams);

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
