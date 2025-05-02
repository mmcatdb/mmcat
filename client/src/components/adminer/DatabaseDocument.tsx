import { useEffect, useState } from 'react';
import { Divider } from '@nextui-org/react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import { getDocumentFromGraphData } from '@/components/adminer/reshapeData';
import type { Datasource } from '@/types/datasource/Datasource';
import type { DocumentResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DatabaseDocumentProps = Readonly<{
    fetchedData: DocumentResponse | GraphResponse;
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DatabaseDocument({ fetchedData, kindReferences, kind, datasourceId, datasources }: DatabaseDocumentProps) {
    const [ documentData, setDocumentData ] = useState<DocumentResponse>();

    useEffect(() => {
        setDocumentData(fetchedData.type === 'graph' ? getDocumentFromGraphData(fetchedData) : fetchedData);
    }, [ fetchedData ]);

    return (
        <>
            {documentData && documentData.data.length > 0 ? (
                <div className='grow mt-2'>
                    {documentData.data.map((value, index) =>
                        <div key={index}>
                            <DocumentComponent valueKey={null} value={value} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources}/>

                            {(index != documentData.data.length - 1) && <Divider className='my-4'/> }
                        </div>,
                    )}
                </div>
            ) : (
                <span>No rows to display.</span>
            )}
        </>
    );
}
