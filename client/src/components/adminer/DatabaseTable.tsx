import { useEffect } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { ReferenceComponent } from '@/components/adminer/ReferenceComponent';
import type { Datasource } from '@/types/datasource/Datasource';
import type { TableResponse, GraphResponse, GraphResponseData } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

function formatCellValue(value: unknown): string {
    return typeof value === 'string' ? value : JSON.stringify(value, null, 2);
}

type DatabaseTableProps = Readonly<{
    fetchedData: TableResponse | GraphResponse;
    setItemCount: (itemCount: number) => void;
    kindReferences: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function DatabaseTable({ fetchedData, setItemCount, kindReferences, kind, datasourceId, datasources }: DatabaseTableProps ) {
    useEffect(() => {
        const count = fetchedData?.metadata.itemCount;
        count ? setItemCount(count) : setItemCount(0);
    }, [ fetchedData ]);

    if (fetchedData === undefined || fetchedData.data.length === 0)
        return <p>No rows to display.</p>;

    // If the data are for graph database, we want to display just properties in the table view
    if (fetchedData.data.every((item: Record<string, string> | GraphResponseData) => 'properties' in item)){
        const modifiedData = { metadata: fetchedData.metadata, data: [] } as TableResponse;

        for (const element of fetchedData.data) {
            const { properties, ...rest } = element;
            modifiedData.data.push({
                ...rest,
                ...(properties as Record<string, string>),
            } as Record<string, string>);
        }

        fetchedData = modifiedData;
    }

    const keys: string[] = typeof fetchedData.data[0] === 'object' ? Object.keys(fetchedData.data[0]) : [ 'Value' ];
    const propertyNames: string[] = fetchedData.data.length > 0 ? keys : [];

    return (
        <div>
            {fetchedData && (
                <Table isStriped isCompact aria-label='Table'>
                    <TableHeader>
                        {propertyNames.map(propertyName => (
                            <TableColumn key={propertyName}>{propertyName}</TableColumn>
                        ))}
                    </TableHeader>
                    {TableBodyComponent({ fetchedData: fetchedData, propertyNames: propertyNames, references: kindReferences, kind: kind, datasourceId: datasourceId, datasources: datasources })}
                </Table>
            )
            }
        </div>
    );
}

type TableBodyComponentProps = Readonly<{
    fetchedData: TableResponse | GraphResponse;
    propertyNames: string[];
    references: KindReference[];
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

function TableBodyComponent({ fetchedData, propertyNames, references, kind, datasourceId, datasources }: TableBodyComponentProps ) {
    return (
        <TableBody emptyContent={'No rows to display.'}>
            {fetchedData.data.map((item, index) => (
                <TableRow key={index}>
                    {item && typeof item === 'object' && !Array.isArray(item)
                        ? propertyNames.map(propertyName => (
                            <TableCell key={propertyName}>
                                {formatCellValue(item[propertyName])}
                                {(references.length > 0
                                    && references.some(ref => ref.referencingProperty === propertyName)) && (
                                    <ReferenceComponent references={references} data={item} propertyName={propertyName} kind={kind} datasourceId={datasourceId} datasources={datasources} />
                                )}
                            </TableCell>
                        ))
                        : <TableCell>
                            {formatCellValue(item)}
                        </TableCell>}
                </TableRow>
            ))}
        </TableBody>
    );
}
