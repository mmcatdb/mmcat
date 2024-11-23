import { Spinner, Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { Datasource } from '@/types/datasource';
import { useNavigate } from 'react-router-dom';
import { usePreferences } from '../PreferencesProvider';
import { ConfirmationModal, useSortableData } from '../TableCommon';
import { type SortDescriptor } from '@react-types/shared';
import { useState } from 'react';

type SchemaCategoriesTableProps = {
    categories: Datasource[];
    loading: boolean;
    error: string | null;
    onDeleteCategory: (id: string) => void;
};

export const SchemaCategoriesTable = ({ categories, loading, error, onDeleteCategory }: SchemaCategoriesTableProps) => {
    const { sortedData: sortedCategories, sortDescriptor, setSortDescriptor } = useSortableData(categories, {
        column: 'label',
        direction: 'ascending',
    });

    const handleSortChange = (newSortDescriptor: SortDescriptor) => {
        setSortDescriptor(newSortDescriptor);
    };

    if (loading) {
        return (
            <div>
                <Spinner />
            </div>
        );
    }

    // TODO: error component
    if (error) 
        return <p>{error}</p>;

    return (
        <div>
            <CategoriesTable
                categories={sortedCategories}
                onDeleteCategory={onDeleteCategory}
                sortDescriptor={sortDescriptor}
                onSortChange={handleSortChange}
            />
        </div>
    );
};

type CategoriesTableProps = {
  categories: Datasource[];
  onDeleteCategory: (id: string) => void;
  sortDescriptor: SortDescriptor;
  onSortChange: (sortDescriptor: SortDescriptor) => void;
};

function CategoriesTable({ categories, onDeleteCategory, sortDescriptor, onSortChange }: CategoriesTableProps) {
    const { showTableIDs } = usePreferences().preferences;
    const navigate = useNavigate();
    const [ selectedCategoryId, setSelectedCategoryId ] = useState<string | null>(null);
    const [ isModalOpen, setModalOpen ] = useState<boolean>(false);

    const handleRowAction = (key: React.Key) => {
        navigate(`/category/${key}`, {});
    };

    const handleDeleteClick = (id: string) => {
        setSelectedCategoryId(id);
        setModalOpen(true);
    };

    const closeModal = () => {
        setSelectedCategoryId(null);
        setModalOpen(false);
    };

    const confirmDelete = () => {
        if (selectedCategoryId)
            onDeleteCategory(selectedCategoryId);

        setModalOpen(false);
    };


    return (
        <>
            <Table 
                aria-label='Datasource Table'
                onRowAction={handleRowAction}
                sortDescriptor={sortDescriptor}
                onSortChange={onSortChange}
                removeWrapper
                isCompact
            >
                <TableHeader>
                    {[
                        ...(showTableIDs
                            ? [
                                <TableColumn key='id' allowsSorting>
                                    ID
                                </TableColumn>,
                            ]
                            : []),
                        <TableColumn key='label' allowsSorting>
                            Label
                        </TableColumn>,
                        <TableColumn key='actions'>Actions</TableColumn>,
                    ]}
                </TableHeader>
                <TableBody emptyContent={'No rows to display.'}>
                    {categories.map((category) => (
                        <TableRow
                            key={category.id}
                            className='hover:bg-zinc-100 focus:bg-zinc-200 dark:hover:bg-zinc-800 dark:focus:bg-zinc-700 cursor-pointer'
                        >
                            {[
                                ...(showTableIDs
                                    ? [ <TableCell key='id'>{category.id}</TableCell> ]
                                    : []),
                                <TableCell key='label'>{category.label}</TableCell>,
                                <TableCell key='actions'>
                                    <Button
                                        isIconOnly
                                        aria-label='Delete'
                                        color='danger'
                                        variant='light'
                                        onPress={() => handleDeleteClick(category.id)}
                                    >
                                        <TrashIcon className='w-5 h-5' />
                                    </Button>
                                </TableCell>,
                            ]}
                        </TableRow>
                    ))}
                </TableBody>
            </Table>

            <ConfirmationModal
                isOpen={isModalOpen}
                onClose={closeModal}
                onConfirm={confirmDelete}
                title='Confirm Deletion?'
                message='This will permanently delete the selected datasource.'
                confirmButtonText='Yes, Delete'
                cancelButtonText='Cancel'
                confirmButtonColor='danger'
            />
        </>
    );
}
