import { useMemo } from 'react';
import { Divider } from '@heroui/react';
import { DocumentComponent } from '@/components/adminer/DocumentComponent';
import { getDocumentFromGraphData } from '@/components/adminer/reshapeData';
import type { Datasource } from '@/types/Datasource';
import type { DocumentResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DatabaseDocumentProps = {
    /** The data to display. */
    data: DocumentResponse | GraphResponse;
    /** References from and to the current kind. */
    kindReferences: KindReference[];
    /** Name of the current kind. */
    kind: string;
    /** The id of selected datasource. */
    datasourceId: Id;
    /** All active datasources. */
    datasources: Datasource[];
};

/**
 * Component for displaying data in document
 */
export function DatabaseDocument({ data, kindReferences, kind, datasourceId, datasources }: DatabaseDocumentProps) {
    const documentData = useMemo(() => {
        if (data.type === 'graph')
            return getDocumentFromGraphData(data);

        return data;
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
