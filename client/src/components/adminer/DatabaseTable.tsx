import { useFetchData } from '@/components/adminer/useFetchData';
import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell } from '@nextui-org/react';

type DatabaseTableProps = {
    apiUrl: string;
};

export const DatabaseTable: React.FC<DatabaseTableProps> = ({ apiUrl }) => {
    const { data, loading, error } = useFetchData(apiUrl);

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    if (error) {
        return <p>{error}</p>;
    }

    const columns: string[] = data.length > 0 
        ? typeof data[0] === 'object' ? Object.keys(data[0]) : ['Value']
        : [];

    return (
        <div>
            <Table aria-label='Database'>
                <TableHeader>
                    {columns.map((column) => (
                        <TableColumn key={column}>{column}</TableColumn>
                    ))}
                </TableHeader>
                <TableBody emptyContent={'No rows to display.'}>
                {data.map((item, index) => (
                    <TableRow key={index}>
                        {typeof item === 'object' && !Array.isArray(item)
                            ? columns.map((column) => (
                                <TableCell key={column}>
                                    {JSON.stringify(item[column as keyof typeof item], null, 2)}
                                </TableCell>
                              ))
                            : <TableCell>{JSON.stringify(item, null, 2)}</TableCell>}
                    </TableRow>
                ))}
            </TableBody>
            </Table>
        </div>
    );
};
