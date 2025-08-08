import { useMemo } from 'react';
import { DocumentDisplay } from '@/components/adminer/dataView/DocumentDisplay';
import { getDocumentFromGraphData } from '@/components/adminer/reshapeData';
import type { Datasource } from '@/types/Datasource';
import { View, type DocumentResponse, type GraphResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';
import { CopyToClipboardButton } from '@/components/CopyToClipboardButton';

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

    console.log({ data: documentData.data });

    return (
        <div className='space-y-1'>
            {documentData.data.map((value, index) => (
                <div key={index} className='relative min-w-4xl px-4 py-2 rounded-lg bg-content1 odd:bg-default-100'>
                    <DocumentDisplay
                        property={undefined}
                        value={value}
                        kindReferences={kindReferences}
                        kind={kind}
                        datasourceId={datasourceId}
                        datasources={datasources}
                    />

                    {/* It's ok this overlaps with the content because the top row is always the `{` character so most of the row is empty. */}
                    <div className='absolute top-2 right-2 select-none'>
                        <CopyToClipboardButton
                            textToCopy={() => JSON.stringify(value, null, 4)}
                            title={`Copy document ${index}`}
                            className='px-2 leading-5 hover:opacity-80'
                        >
                            {index}
                        </CopyToClipboardButton>
                    </div>
                </div>
            ))}
        </div>
    );
}
