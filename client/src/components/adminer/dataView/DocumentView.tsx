import { useMemo } from 'react';
import { DocumentDisplay } from '@/components/adminer/dataView/DocumentDisplay';
import { getDocumentFromGraphData } from '@/components/adminer/reshapeData';
import type { Datasource } from '@/types/Datasource';
import { View, type DocumentResponse, type GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type DocumentViewProps = {
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
export function DocumentView({ data, kindReferences, kind, datasourceId, datasources }: DocumentViewProps) {
    const documentData = useMemo(() => {
        if (data.type === View.graph)
            return getDocumentFromGraphData(data);

        return data;
    }, [ data ]);

    if (!documentData || documentData.data.length === 0)
        return (<span>No rows to display.</span>);

    return (
        <div className='space-y-1'>
            {documentData.data.map((value, index) => (
                <div key={index} className='pl-4 pr-8 py-2 rounded-lg bg-default-100 even:bg-content1'>
                    <DocumentDisplay
                        property={undefined}
                        value={value}
                        kindReferences={kindReferences}
                        kind={kind}
                        datasourceId={datasourceId}
                        datasources={datasources}
                    />
                </div>
            ))}
        </div>
    );
}
