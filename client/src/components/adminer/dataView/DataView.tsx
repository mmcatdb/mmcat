import type { Datasource } from '@/types/Datasource';
import type { Id } from '@/types/id';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import { type DataResponse, type DocumentResponse, type GraphResponse, type TableResponse, View } from '@/types/adminer/DataResponse';
import { TableView } from '@/components/adminer/dataView/TableView';
import { DocumentView } from '@/components/adminer/dataView/DocumentView';
import { GraphView } from '@/components/adminer/dataView/GraphView';

type DataViewProps = {
    /** The selected view. */
    view: View;
    /** The data to display. */
    data: DataResponse;
    /** References from and to the current kind. */
    kindReferences: KindReference[];
    /** Name of the current kind. */
    kindName: string;
    /** The id of selected datasource. */
    datasourceId: Id;
    /** All active datasources. */
    datasources: Datasource[];
};

/**
 * Component that returns view based on the value of variable 'view'
 */
export function DataView({ view, data, kindReferences, kindName, datasourceId, datasources }: DataViewProps) {
    switch (view) {
    case View.table:
        return (
            <div className='grow w-full p-1 overflow-x-auto overflow-y-scroll'>
                <div className='mx-auto w-fit'>
                    <TableView
                        data={data as TableResponse | GraphResponse}
                        kindReferences={kindReferences}
                        kind={kindName}
                        datasourceId={datasourceId}
                        datasources={datasources}
                    />
                </div>
            </div>
        );
    case View.document:
        return (
            <div className='grow w-full p-1 overflow-x-auto overflow-y-scroll'>
                <div className='mx-auto w-fit'>
                    <DocumentView
                        data={data as DocumentResponse | GraphResponse}
                        kindReferences={kindReferences}
                        kind={kindName}
                        datasourceId={datasourceId}
                        datasources={datasources}
                    />
                </div>
            </div>
        );
    case View.graph:
        return (
            <GraphView
                data={data as GraphResponse}
                kind={kindName}
            />
        );
    }
}
