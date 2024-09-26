import { useFetchArrayData } from '@/components/adminer/useFetchArrayData';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';

type DatabaseTableProps = Readonly<{
    apiUrl: string;
}>;

export function DatabaseTable({ apiUrl }: DatabaseTableProps ) {
    const { fetchedData, loading, error } = useFetchArrayData(apiUrl);

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
                                            {typeof item[column as keyof typeof item] === 'string'
                                                ? item[column as keyof typeof item] // Render string without quotes
                                                : JSON.stringify(item[column as keyof typeof item], null, 2)}
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
