import { useEffect } from 'react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import type { Datasource } from '@/types/datasource/Datasource';
import type { DocumentResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DatabaseDocumentProps = Readonly<{
    fetchedData: DocumentResponse | GraphResponse;
    setItemCount: (itemCount: number) => void;
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DatabaseDocument({ fetchedData, setItemCount, kindReferences, kind, datasourceId, datasources }: DatabaseDocumentProps) {
    useEffect(() => {
        const count = fetchedData?.metadata.itemCount;
        count ? setItemCount(count) : setItemCount(0);
    }, [ fetchedData ]);

    return (
        <div>
            {fetchedData && fetchedData.data.length > 0 ? (
                <DocumentComponent valueKey={null} value={fetchedData.data} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources} depth={0}/>
            ) : (
                <span>No rows to display.</span>
            )}
        </div>
    );
}
