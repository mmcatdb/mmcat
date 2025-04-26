import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { View } from '@/types/adminer/View';
import { DatasourceType, type Datasource } from '@/types/datasource/Datasource';
import type { DataResponse, DocumentResponse, GraphResponse, TableResponse } from '@/types/adminer/DataResponse';

type CustomQueryDatabaseViewProps = Readonly<{
    queryResult: DataResponse;
    datasource: Datasource;
    datasources: Datasource[];
    view: View;
}>;

export function CustomQueryDatabaseView({ queryResult, datasource, datasources, view }: CustomQueryDatabaseViewProps) {
    switch (datasource.type) {
    case DatasourceType.mongodb:
        return (
            <DatabaseDocument
                fetchedData={queryResult as DocumentResponse}
                kindReferences={[]}
                kind={''}
                datasourceId={datasource.id}
                datasources={datasources}
            />
        );
    case DatasourceType.neo4j:
        return (
            <>
                {view === View.document ? (
                    <DatabaseDocument
                        fetchedData={queryResult as GraphResponse}
                        kindReferences={[]}
                        kind={''}
                        datasourceId={datasource.id}
                        datasources={datasources}
                    />
                ) : (
                    <DatabaseTable
                        fetchedData={queryResult as GraphResponse}
                        kindReferences={[]}
                        kind={''}
                        datasourceId={datasource.id}
                        datasources={datasources}
                    />
                )}
            </>
        );
    default:
        return (
            <DatabaseTable
                fetchedData={queryResult as TableResponse}
                kindReferences={[]}
                kind={''}
                datasourceId={datasource.id}
                datasources={datasources}
            />
        );
    }
}
