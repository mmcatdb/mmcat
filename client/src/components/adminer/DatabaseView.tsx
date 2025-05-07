import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { DatabaseGraph } from '@/components/adminer/DatabaseGraph';
import { View } from '@/types/adminer/View';
import type { Datasource } from '@/types/datasource';
import type { Id } from '@/types/id';
import type { DataResponse, DocumentResponse, GraphResponse, TableResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';

/**
 * @param view The selected view
 * @param data The data to display
 * @param kindReferences References from and to the current kind
 * @param kindName Name of the current kind
 * @param datasourceId The id of selected datasource
 * @param datasources All active datasources
 */
type DatabaseViewProps = Readonly<{
    view: View;
    data: DataResponse;
    kindReferences: KindReference[];
    kindName: string;
    datasourceId: Id;
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
