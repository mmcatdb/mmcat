import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import { routes } from '@/routes/routes';
import { getHrefFromReference } from '@/components/adminer/URLParamsState';
import type { Datasource } from '@/types/datasource/Datasource';
import type { TableResponse, GraphResponse, GraphResponseData } from '@/types/adminer/DataResponse';
import type { AdminerReference, AdminerReferences } from '@/types/adminer/AdminerReferences';

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
                    {TableBodyComponent({ fetchedData: fetchedData, columns: columns, references: references, referencedProperties: referencedProperties, datasources: datasources })}
                </Table>
            )
            }
        </div>
    );
}

type TableBodyComponentProps = Readonly<{
    fetchedData: TableResponse | GraphResponse;
    columns: string[];
    references: AdminerReferences | undefined;
    referencedProperties: string[];
    datasources: Datasource[];
}>;

function TableBodyComponent({ fetchedData, columns, references, referencedProperties, datasources }: TableBodyComponentProps ) {
    return (
        <TableBody emptyContent={'No rows to display.'}>
            {fetchedData.data.map((item, index) => (
                <TableRow key={index}>
                    {item && typeof item === 'object' && !Array.isArray(item)
                        ? columns.map(column => (
                            <TableCell key={column}>
                                {references && referencedProperties.includes(column) ? (
                                    getLinkReferences(references, column).map((ref, index) => (
                                        <LinkComponent key={index} index={index} reference={ref} data={item} column={column} datasources={datasources} />
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
    );
}

type LinkComponentProps = Readonly<{
    index: number;
    reference: AdminerReference;
    data: Record<string, string> | GraphResponseData;
    column: string;
    datasources: Datasource[];
}>;

function LinkComponent({ index, reference, data, column, datasources }: LinkComponentProps ) {
    return (
        <Link
            key={index}
            to={{ pathname:routes.adminer, search: getHrefFromReference(reference, data, column, datasources) }}
            className='mr-2 hover:underline text-blue-500'
        >
            {formatCellValue(data[column])}
        </Link>
    );
}
