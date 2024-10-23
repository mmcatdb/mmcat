import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/datasource';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

type SchemaCategoriesTableProps = {
    categories: Datasource[];
    loading: boolean;
    error: string | null;
    onDeleteDatasource: (id: string) => void;
};

export const SchemaCategoriesTable = ({ categories, loading, error, onDeleteDatasource }: SchemaCategoriesTableProps) => {
    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    // TODO: error page
    if (error) 
        return <p>{error}</p>;

    return (
        <div>
            <DatasourceTable
                categories={categories}
                onDeleteDatasource={onDeleteDatasource}
            />
        </div>
    );
};

type DatasourceTableProps = {
  categories: Datasource[];
  onDeleteDatasource: (id: string) => void;
};

function DatasourceTable({ categories }: DatasourceTableProps) {
    const navigate = useNavigate();

    const handleRowAction = (key: React.Key) => {
        navigate(`/projects/${key}`, {});
    };

    function handleDeleteClick(id: string): void {
        toast.error('Not implemented yet. Can\'t delete ' + id);
    }

    return (
        <>
            <Table 
                aria-label='Datasource Table'
                onRowAction={handleRowAction}
                removeWrapper
                isCompact
            >
                <TableHeader>
                    <TableColumn key='id'>
                        ID
                    </TableColumn>
                    <TableColumn key='label'>
                        Label
                    </TableColumn>
                    <TableColumn>Actions</TableColumn>
                </TableHeader>
                <TableBody emptyContent={'No rows to display.'}>
                    {categories.map((category) => (
                        <TableRow
                            key={category.id}
                            className='hover:bg-zinc-100 focus:bg-zinc-200 dark:hover:bg-zinc-800 dark:focus:bg-zinc-700 cursor-pointer'
                        >
                            <TableCell>{category.id}</TableCell>
                            <TableCell>{category.label}</TableCell>
                            <TableCell>
                                <Button
                                    isIconOnly
                                    aria-label='Delete'
                                    color='danger'
                                    variant='light'
                                    onPress={() => handleDeleteClick(category.id)}
                                >
                                    <TrashIcon className='w-5 h-5' />
                                </Button>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </>
    );
}

export default SchemaCategoriesTable;
