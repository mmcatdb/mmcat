import { useEffect } from 'react';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import type { BackendTableResponse } from '@/types/adminer/BackendResponse';
import type { FetchKindParams } from '@/types/adminer/FetchParams';
import { useFetchData } from './useFetchData';

type DatabaseTableProps = Readonly<{
    urlParams: FetchKindParams;
    setRowCount: (rowCount: number | undefined) => void;
}>;

export function DatabaseTable({ urlParams, setRowCount }: DatabaseTableProps ) {
    let { fetchedData, loading, error } = useFetchData(urlParams);

    useEffect(() => {
        setRowCount(fetchedData?.metadata.rowCount);
    }, [ fetchedData, setRowCount ]);

    if (loading) {
        return (
            <div className='h-10 flex items-center justify-center'>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;

    if (fetchedData === undefined)
        return <p>No data to display.</p>;

    // If the data are for graph database, we want to display just properties in the table view
    if (fetchedData.data.every((item: any) => 'properties' in item)){
        const modifiedData = { metadata: fetchedData.metadata, data: [] } as BackendTableResponse;

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
