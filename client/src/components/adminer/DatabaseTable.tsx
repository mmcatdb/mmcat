import { useEffect } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import type { TableResponse, GraphResponse } from '@/types/adminer/DataResponse';
import type { AdminerReference } from '@/types/adminer/AdminerReference';

type DatabaseTableProps = Readonly<{
    fetchedData: TableResponse | GraphResponse;
    setItemCount: (itemCount: number) => void;
    references: AdminerReference | undefined;
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

    return (
        <div>
            {fetchedData && (
                <Table isStriped isCompact aria-label='Table'>
                    <TableHeader>
                        {columns.map((column) => (
                            <TableColumn key={column}>{column}</TableColumn>
                        ))}
                    </TableHeader>
                    <TableBody emptyContent={'No rows to display.'}>
                        {fetchedData.data.map((item, index) => (
                            <TableRow key={index}>
                                {item && typeof item === 'object' && !Array.isArray(item)
                                    ? columns.map((column) => (
                                        <TableCell key={column}>
                                            {typeof item[column] === 'string'
                                                ? item[column] // Render string without quotes
                                                : JSON.stringify(item[column], null, 2)}
                                        </TableCell>
                                    ))
                                    : <TableCell>
                                        {typeof item === 'string'
                                            ? item // Render string without quotes
                                            : JSON.stringify(item, null, 2)}
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
