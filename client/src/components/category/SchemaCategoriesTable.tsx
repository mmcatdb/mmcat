import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@heroui/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { SchemaCategoryInfo } from '@/types/schema';
import { useNavigate } from 'react-router-dom';
import { usePreferences } from '../context/PreferencesProvider';
import { ConfirmationModal } from '../common/tableComponents';
import { useSortable } from '../common/tableUtils';
import { useState } from 'react';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { toast } from 'react-toastify';
import { type Id } from '@/types/id';
import { useCached } from '../hooks/useCached';

type SchemaCategoriesTableProps = {
    categories: SchemaCategoryInfo[];
    onDeleteCategory: (id: Id) => void;
};

export function SchemaCategoriesTable({ categories, onDeleteCategory }: SchemaCategoriesTableProps) {
    const { showTableIDs } = usePreferences().preferences;

    const [ toDelete, setToDelete ] = useState<SchemaCategoryInfo>();
    const toDeleteCache = useCached(toDelete);
    const [ isFetching, setIsFetching ] = useState<boolean>(false);

    async function confirmDelete() {
        if (!toDelete)
            return;

        setIsFetching(true);
        const response = await api.schemas.deleteCategory({ id: toDelete.id });
        setIsFetching(false);
        if (!response.status) {
            toast.error(`Error deleting schema category ${toDelete.label}.`);
            return;
        }

        toast.success(`Schema category ${toDelete.label} deleted successfully!`);
        onDeleteCategory(toDelete.id);
        setToDelete(undefined);
    }

    const { sorted, sortDescriptor, setSortDescriptor } = useSortable(categories, {
        column: 'label',
        direction: 'ascending',
    });

    const navigate = useNavigate();

    function handleRowAction(key: React.Key) {
        navigate(routes.category.index.resolve({ categoryId: String(key) }));
    }

    return (<>
        <ConfirmationModal
            isOpen={!!toDelete}
            onClose={() => setToDelete(undefined)}
            onConfirm={confirmDelete}
            isFetching={isFetching}
            title='Confirm Deletion?'
            message={`The schema category ${toDeleteCache?.label} will be permanently deleted.`}
            confirmButtonText='Yes, Delete'
            cancelButtonText='Cancel'
            confirmButtonColor='danger'
        />

        <Table
            aria-label='Schema Categories Table'
            onRowAction={handleRowAction}
            sortDescriptor={sortDescriptor}
            onSortChange={setSortDescriptor}
        >
            <TableHeader>
                {[
                    ...(showTableIDs ? [
                        <TableColumn key='id' allowsSorting>
                            ID
                        </TableColumn>,
                    ] : []),
                    <TableColumn key='label' allowsSorting>
                        Label
                    </TableColumn>,
                    <TableColumn key='version' allowsSorting>
                        System Version
                    </TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>
            <TableBody emptyContent='No rows to display.' items={sorted}>
                {category => (
                    <TableRow key={category.id} className='cursor-pointer hover:bg-default-100 focus:bg-default-200'>
                        {[
                            ...(showTableIDs ? [
                                <TableCell key='id'>{category.id}</TableCell>,
                            ] : []),
                            <TableCell key='label'>{category.label}</TableCell>,
                            <TableCell key='version'>{category.systemVersionId}</TableCell>,
                            <TableCell key='actions'>
                                <Button
                                    isIconOnly
                                    aria-label='Delete'
                                    color='danger'
                                    variant='light'
                                    onPress={() => setToDelete(category)}
                                >
                                    <TrashIcon className='size-5' />
                                </Button>
                            </TableCell>,
                        ]}
                    </TableRow>
                )}
            </TableBody>
        </Table>
    </>);
}
