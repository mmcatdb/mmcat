import { useEffect } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Link } from '@nextui-org/react';
import { getHrefFromReference } from '@/components/adminer/URLParamsState';
import type { Datasource } from '@/types/datasource/Datasource';
import type { TableResponse, GraphResponse, GraphResponseData } from '@/types/adminer/DataResponse';
import type { AdminerReferences } from '@/types/adminer/AdminerReferences';

function formatCellValue(value: unknown): string {
    return typeof value === 'string' ? value : JSON.stringify(value, null, 2);
}

function getLinkReferences(references: AdminerReferences, referencedProperty: string): AdminerReferences {
    return Object.values(references).filter(ref => ref.referencedProperty === referencedProperty);
}

type DatabaseTableProps = Readonly<{
    fetchedData: TableResponse | GraphResponse;
    setItemCount: (itemCount: number) => void;
    references: AdminerReferences | undefined;
    datasources: Datasource[];
}>;

export function DatabaseTable({ fetchedData, setItemCount, references, datasources }: DatabaseTableProps ) {
    useEffect(() => {
        const count = fetchedData?.metadata.itemCount;
        count ? setItemCount(count) : setItemCount(0);
    }, [ fetchedData ]);

    if (fetchedData === undefined || fetchedData.data.length === 0)
        return <p>No rows to display.</p>;

    // If the data are for graph database, we want to display just properties in the table view
    if (fetchedData.data.every((item: Record<string, string> | GraphResponseData) => 'properties' in item)){
        const modifiedData = { metadata: fetchedData.metadata, data: [] } as TableResponse;

        for (const element of fetchedData.data)
            modifiedData.data.push(element.properties as Record<string, string>);

        fetchedData = modifiedData;
    }

    const keys: string[] = typeof fetchedData.data[0] === 'object' ? Object.keys(fetchedData.data[0]) : [ 'Value' ];
    const columns: string[] = fetchedData.data.length > 0 ? keys : [];
    const referencedProperties: string[] = references ? Object.values(references).map(ref => ref.referencedProperty) : [];

    return (
        <div>
            {fetchedData && (
                <Table isStriped isCompact aria-label='Table'>
                    <TableHeader>
                        {columns.map(column => (
                            <TableColumn key={column}>{column}</TableColumn>
                        ))}
                    </TableHeader>
                    <TableBody emptyContent={'No rows to display.'}>
                        {fetchedData.data.map((item, index) => (
                            <TableRow key={index}>
                                {item && typeof item === 'object' && !Array.isArray(item)
                                    ? columns.map(column => (
                                        <TableCell key={column}>
                                            {references && referencedProperties.includes(column) ? (
                                                getLinkReferences(references, column).map((ref, index) => (
                                                    <Link key={index} href={getHrefFromReference(ref, item, column, datasources)}>
                                                        {formatCellValue(item[column])}
                                                    </Link>
                                                ))
                                            ) : (
                                                formatCellValue(item[column])
                                            )}
                                        </TableCell>
                                    ))
                                    : <TableCell>
                                        {formatCellValue(item)}
                                    </TableCell>}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            )
            }
        </div>
    );
}
