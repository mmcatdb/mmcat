import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { DatabaseGraph } from '@/components/adminer/DatabaseGraph';
import { View } from '@/types/adminer/View';
import type { Datasource } from '@/types/datasource';
import type { Id } from '@/types/id';
import type { DataResponse, DocumentResponse, GraphResponse, TableResponse } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';

type DatabaseViewProps = Readonly<{
    view: View;
    fetchedData: DataResponse;
    kindReferences: KindReference[];
    kindName: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DatabaseView({ view, fetchedData, kindReferences, kindName, datasourceId, datasources }: DatabaseViewProps) {
    switch (view) {
    case View.table:
        return (
            <DatabaseTable
                fetchedData={fetchedData as TableResponse | GraphResponse}
                kindReferences={kindReferences}
                kind={kindName}
                datasourceId={datasourceId}
                datasources={datasources}
            />
        );
    case View.document:
        return (
            <DatabaseDocument
                fetchedData={fetchedData as DocumentResponse | GraphResponse}
                kindReferences={kindReferences}
                kind={kindName}
                datasourceId={datasourceId}
                datasources={datasources}
            />
        );
    case View.graph:
        return (
            <DatabaseGraph
                fetchedData={fetchedData as GraphResponse}
                kind={kindName}
            />
        );
    }
}
