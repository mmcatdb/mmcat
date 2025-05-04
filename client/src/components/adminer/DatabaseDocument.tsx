import { useMemo, useState } from 'react';
import { Divider } from '@nextui-org/react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import { getDocumentFromGraphData } from '@/components/adminer/reshapeData';
import type { Datasource } from '@/types/datasource/Datasource';
import type { DocumentResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DatabaseDocumentProps = Readonly<{
    data: DocumentResponse | GraphResponse;
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DatabaseDocument({ data, kindReferences, kind, datasourceId, datasources }: DatabaseDocumentProps) {
    const [ documentData, setDocumentData ] = useState<DocumentResponse>();

    useMemo(() => {
        setDocumentData(data.type === 'graph' ? getDocumentFromGraphData(data) : data);
    }, [ data ]);

    if (!documentData || documentData.data.length === 0)
        return (<span>No rows to display.</span>);


    return (
        <div className='grow mt-2'>
            {documentData.data.map((value, index) =>
                <div key={index}>
                    <DocumentComponent valueKey={undefined} value={value} kindReferences={kindReferences} kind={kind} datasourceId={datasourceId} datasources={datasources}/>

                    {(index != documentData.data.length - 1) && <Divider className='my-4'/> }
                </div>,
            )}
        </div>
    );
}
