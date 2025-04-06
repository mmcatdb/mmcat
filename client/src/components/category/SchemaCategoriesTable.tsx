import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button } from '@nextui-org/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import type { SchemaCategoryInfo } from '@/types/schema';
import { useNavigate } from 'react-router-dom';
import { usePreferences } from '../PreferencesProvider';
import { ConfirmationModal, useSortableData } from '../TableCommon';
import { type SortDescriptor } from '@react-types/shared';
import { useMemo, useState } from 'react';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { toast } from 'react-toastify';
import { type Id } from '@/types/id';
import { useCached } from '../hooks/UseCached';

type SchemaCategoriesTableProps = {
    categories: SchemaCategoryInfo[];
    onDeleteCategory: (id: Id) => void;
};

export function SchemaCategoriesTable({ categories, onDeleteCategory }: SchemaCategoriesTableProps) {
    const { sortedData: sortedCategories, sortDescriptor, setSortDescriptor } = useSortableData(categories, {
        column: 'label',
        direction: 'ascending',
    });

    const handleSortChange = (newSortDescriptor: SortDescriptor) => {
        setSortDescriptor(newSortDescriptor);
    };

    return (
        <CategoriesTable
            categories={sortedCategories}
            onDeleteCategory={onDeleteCategory}
            sortDescriptor={sortDescriptor}
            onSortChange={handleSortChange}
        />
    );
}

type CategoriesTableProps = {
  categories: SchemaCategoryInfo[];
  onDeleteCategory: (id: Id) => void;
  sortDescriptor: SortDescriptor;
  onSortChange: (sortDescriptor: SortDescriptor) => void;
};

function CategoriesTable({ categories, onDeleteCategory, sortDescriptor, onSortChange }: CategoriesTableProps) {
    const { showTableIDs } = usePreferences().preferences;
    const navigate = useNavigate();

    function handleRowAction(key: React.Key) {
        navigate(routes.categories + `/${key}`);
    }

    const [ deletingCategoryId, setDeletingCategoryId ] = useState<Id>();
    const rawDeletingCategory = useMemo(() => categories.find(c => c.id === deletingCategoryId), [ categories, deletingCategoryId ]);
    const deletingCategory = useCached(rawDeletingCategory);
    const [ isDeleting, setIsDeleting ] = useState<boolean>(false);

    async function confirmDelete() {
        if (!deletingCategory)
            return;

        setIsDeleting(true);
        const response = await api.schemas.deleteCategory(deletingCategory);
        setIsDeleting(false);
        if (!response.status) {
            toast.error(`Error deleting schema category ${deletingCategory.label}.`);
            return;
        }

        toast.success(`Schema category ${deletingCategory.label} deleted successfully!`);
        onDeleteCategory(deletingCategory.id);

        setDeletingCategoryId(undefined);
    }

    return (<>
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
                {categories.map(category => (
                    <TableRow
                        key={category.id}
                        className='cursor-pointer hover:bg-default-100 focus:bg-default-200'
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
                                    onPress={() => setDeletingCategoryId(category.id)}
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
            isOpen={!!rawDeletingCategory}
            onClose={() => setDeletingCategoryId(undefined)}
            onConfirm={confirmDelete}
            isFetching={isDeleting}
            title='Confirm Deletion?'
            message={`The schema category ${deletingCategory?.label} will be permanently deleted.`}
            confirmButtonText='Yes, Delete'
            cancelButtonText='Cancel'
            confirmButtonColor='danger'
        />
    </>);
}
