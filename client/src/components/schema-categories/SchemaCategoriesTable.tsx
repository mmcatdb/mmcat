import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { SchemaCategoryInfo } from '@/types/schema';
import { useNavigate } from 'react-router-dom';
import { usePreferences } from '../PreferencesProvider';
import { ConfirmationModal, useSortableData } from '../TableCommon';
import { type SortDescriptor } from '@react-types/shared';
import { useState } from 'react';
import { ErrorPage, LoadingPage } from '@/pages/errorPages';
import { cn } from '@/components/utils';

type SchemaCategoriesTableProps = {
    categories: SchemaCategoryInfo[];
    loading: boolean;
    error: boolean;
    onDeleteCategory: (id: string) => void;
};

export function SchemaCategoriesTable({ categories, loading, error, onDeleteCategory }: SchemaCategoriesTableProps) {
    const { sortedData: sortedCategories, sortDescriptor, setSortDescriptor } = useSortableData(categories, {
        column: 'label',
        direction: 'ascending',
    });

    const handleSortChange = (newSortDescriptor: SortDescriptor) => {
        setSortDescriptor(newSortDescriptor);
    };

    if (loading) {
        return (
            <LoadingPage />
        );
    }

    if (error) {
        return (
            <ErrorPage />
        );
    }

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
}

type CategoriesTableProps = {
  categories: SchemaCategoryInfo[];
  onDeleteCategory: (id: string) => void;
  sortDescriptor: SortDescriptor;
  onSortChange: (sortDescriptor: SortDescriptor) => void;
};

function CategoriesTable({ categories, onDeleteCategory, sortDescriptor, onSortChange }: CategoriesTableProps) {
    const { theme, showTableIDs } = usePreferences().preferences;
    const navigate = useNavigate();
    const [ selectedCategoryId, setSelectedCategoryId ] = useState<string>();
    const [ isModalOpen, setModalOpen ] = useState<boolean>(false);

    function handleRowAction(key: React.Key) {
        navigate(`/category/${key}`, {});
    }

    function handleDeleteClick(id: string) {
        setSelectedCategoryId(id);
        setModalOpen(true);
    }

    function closeModal() {
        setSelectedCategoryId(undefined);
        setModalOpen(false);
    }

    function confirmDelete() {
        if (selectedCategoryId)
            onDeleteCategory(selectedCategoryId);

        setModalOpen(false);
    }


    return (
        <div>
            <Table 
                aria-label='Schema Categories Table'
                onRowAction={handleRowAction}
                sortDescriptor={sortDescriptor}
                onSortChange={onSortChange}
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
                        <TableColumn key='version' allowsSorting>
                            System Version
                        </TableColumn>,
                        <TableColumn key='actions'>Actions</TableColumn>,
                    ]}
                </TableHeader>
                <TableBody emptyContent={'No rows to display.'}>
                    {categories.map((category) => (
                        <TableRow
                            key={category.id}
                            className={cn('cursor-pointer', 
                                theme === 'dark' ? 'hover:bg-zinc-800 focus:bg-zinc-700' : 'hover:bg-zinc-100 focus:bg-zinc-200')}
                        >
                            {[
                                ...(showTableIDs
                                    ? [ <TableCell key='id'>{category.id}</TableCell> ]
                                    : []),
                                <TableCell key='label'>{category.label}</TableCell>,
                                <TableCell key='version'>{category.systemVersionId}</TableCell>,
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
                message='This will permanently delete the selected schema category.'
                confirmButtonText='Yes, Delete'
                cancelButtonText='Cancel'
                confirmButtonColor='danger'
            />
        </div>
    );
}
