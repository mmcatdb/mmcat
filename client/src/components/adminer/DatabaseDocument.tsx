import { useCallback, useEffect } from 'react';
import { Spinner } from '@nextui-org/react';
import { useFetchData } from './useFetchData';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import type { FetchKindParams } from '@/types/adminer/FetchParams';
import { type AdminerReference } from '@/types/adminer/AdminerReference';
import { api } from '@/api';

type DatabaseDocumentProps = Readonly<{
    urlParams: FetchKindParams;
    setItemCount: (itemCount: number) => void;
    references: AdminerReference | undefined;
}>;

export function DatabaseDocument({ urlParams, setItemCount, references }: DatabaseDocumentProps) {
    const fetchFunction = useCallback(() => {
        return api.adminer.getKind({ datasourceId: urlParams.datasourceId, kindId: urlParams.kindId }, urlParams.queryParams);
    }, [ urlParams ]);

    const { fetchedData, loading, error } = useFetchData(fetchFunction);

    useEffect(() => {
        const count = fetchedData?.metadata.itemCount;
        count ? setItemCount(count) : setItemCount(0);
    }, [ fetchedData ]);

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
        <div>
            {fetchedData && fetchedData.data.length > 0 ? (
                <DocumentComponent value={fetchedData.data} depth={0}/>
            ) : (
                <span>No rows to display.</span>
            )}
        </div>
    );
}
