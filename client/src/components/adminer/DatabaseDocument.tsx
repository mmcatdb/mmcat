import { useEffect } from 'react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import type { DocumentResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { AdminerReferences } from '@/types/adminer/AdminerReferences';

type DatabaseDocumentProps = Readonly<{
    fetchedData: DocumentResponse | GraphResponse;
    setItemCount: (itemCount: number) => void;
    references: AdminerReferences | undefined;
}>;

export function DatabaseDocument({ fetchedData, setItemCount, references }: DatabaseDocumentProps) {
    useEffect(() => {
        const count = fetchedData?.metadata.itemCount;
        count ? setItemCount(count) : setItemCount(0);
    }, [ fetchedData ]);

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
