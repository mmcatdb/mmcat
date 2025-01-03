import { useEffect } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Link } from '@nextui-org/react';
import { getURLParamsFromState } from './URLParamsState';
import { Operator } from '@/types/adminer/Operators';
import { View } from '@/types/adminer/View';
import type { TableResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { AdminerReference, AdminerReferences } from '@/types/adminer/AdminerReferences';
import type { AdminerState } from '@/types/adminer/Reducer';

function formatCellValue(value: any): string {
    return typeof value === 'string' ? value : JSON.stringify(value, null, 2);
}

function getLinkReferences(references: AdminerReferences, referencedProperty: string): AdminerReferences {
    return Object.values(references).filter(ref => ref.referencedProperty === referencedProperty);
}

function getHref(ref: AdminerReference, item: Record<string, any>, column: string): string {
    const state: AdminerState = {
        form: { limit: 50, filters: [] },
        active: {
            limit: 50,
            filters: [
                {
                    id: 0,
                    propertyName: ref.referencingProperty,
                    operator: Operator.Equal,
                    propertyValue: item[column] as string,
                },
            ],
        },
        datasourceId: ref.datasourceId,
        kindName: ref.referencingKindName,
        view: View.table,
    };

    const urlParams = getURLParamsFromState(state);

    return `adminer?${urlParams.toString()}`;
}

type DatabaseTableProps = Readonly<{
    fetchedData: TableResponse | GraphResponse;
    setItemCount: (itemCount: number) => void;
    references: AdminerReferences | undefined;
}>;

export function DatabaseTable({ fetchedData, setItemCount, references }: DatabaseTableProps ) {
    useEffect(() => {
        const count = fetchedData?.metadata.itemCount;
        count ? setItemCount(count) : setItemCount(0);
    }, [ fetchedData ]);

    if (fetchedData === undefined || fetchedData.data.length === 0)
        return <p>No rows to display.</p>;

    // If the data are for graph database, we want to display just properties in the table view
    if (fetchedData.data.every((item: any) => 'properties' in item)){
        const modifiedData = { metadata: fetchedData.metadata, data: [] } as TableResponse;

        for (const element of fetchedData.data)
            modifiedData.data.push(element.properties);

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
                                                    <Link key={index} href={getHref(ref, item, column)}>
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
