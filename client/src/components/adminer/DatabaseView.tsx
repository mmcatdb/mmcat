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
    // FIXME V tuto chvíli nevíte nic o fečování - data mohla přijít odkudkoliv. Takže už bych tomu neříkal fetchedData.
    fetchedData: DataResponse;
    // FIXME Proč jsou references optional a následně níže povinné? Pokud můžou být undefined, tak by to mělo být řešeno. Pokud nemůžou, neměly by být ani v těchto props.
    // To samé platí pro datasourceId a datasources.
    kindReferences: KindReference[] | undefined;
    kindName: string;
    datasourceId: Id | undefined;
    datasources: Datasource[] | undefined;
}>;

export function DatabaseView({ view, fetchedData, kindReferences, kindName, datasourceId, datasources }: DatabaseViewProps) {
    switch (view) {
    case View.table:
        return (
            <DatabaseTable
                fetchedData={fetchedData as TableResponse | GraphResponse}
                kindReferences={kindReferences!}
                kind={kindName}
                datasourceId={datasourceId!}
                datasources={datasources!}
            />
        );
    case View.document:
        return (
            <DatabaseDocument
                fetchedData={fetchedData as DocumentResponse | GraphResponse}
                kindReferences={kindReferences!}
                kind={kindName}
                datasourceId={datasourceId!}
                datasources={datasources!}
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
