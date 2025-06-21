import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { DatabaseGraph } from '@/components/adminer/DatabaseGraph';
import { View } from '@/types/adminer/View';
import type { Datasource } from '@/types/datasource';
import type { Id } from '@/types/id';
import type { DataResponse, DocumentResponse, GraphResponse, TableResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';

type DatabaseViewProps = Readonly<{
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
}>;

/**
 * Component that returns view based on the value of variable 'view'
 */
export function DatabaseView({ view, data, kindReferences, kindName, datasourceId, datasources }: DatabaseViewProps) {
    switch (view) {
    case View.table:
        return (
            <DatabaseTable
                data={data as TableResponse | GraphResponse}
                kindReferences={kindReferences}
                kind={kindName}
                datasourceId={datasourceId}
                datasources={datasources}
            />
        );
    case View.document:
        return (
            <DatabaseDocument
                data={data as DocumentResponse | GraphResponse}
                kindReferences={kindReferences}
                kind={kindName}
                datasourceId={datasourceId}
                datasources={datasources}
            />
        );
    case View.graph:
        return (
            <DatabaseGraph
                data={data as GraphResponse}
                kind={kindName}
            />
        );
    }
}
