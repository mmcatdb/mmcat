import { useEffect } from 'react';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';
import type { BackendTableResponse, BackendDocumentResponse, BackendGraphResponse } from '@/types/adminer/BackendResponse';

type DatabaseTableProps = Readonly<{
    apiUrl: string;
    fetchData: (url: string) => {
        fetchedData: BackendTableResponse | BackendDocumentResponse | BackendGraphResponse | null;
        loading: boolean;
        error: string | null;
    };
    setRowCount: (rowCount: number | undefined) => void;
}>;

export function DatabaseTable({ apiUrl, fetchData, setRowCount }: DatabaseTableProps ) {
    const { fetchedData, loading, error } = fetchData(apiUrl);

    useEffect(() => {
        setRowCount(fetchedData?.metadata.rowCount);
    }, [ fetchedData, setRowCount ]);

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;

    if (fetchedData === null)
        return <p>No data to display.</p>;

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
