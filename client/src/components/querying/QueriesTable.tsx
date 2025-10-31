import { useState } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button, type SortDescriptor, Tooltip } from '@heroui/react';
import { TrashIcon } from '@heroicons/react/24/outline';
import { type Query } from '@/types/query';
import { useNavigate } from 'react-router-dom';
import { usePreferences } from '../PreferencesProvider';
import { ConfirmationModal, useSortableData } from '../TableCommon';
import { routes } from '@/routes/routes';
import { toast } from 'react-toastify';
import { type Id } from '@/types/id';
import { useCached } from '../hooks/useCached';
import { api } from '@/api';
import { useCategoryInfo } from '../CategoryInfoProvider';

type QueriesTableProps = {
    queries: Query[];
    onDelete: (id: Id) => void;
};

/**
 * Renders a sortable table of queries with delete functionality.
 */
export function QueriesTable({ queries, onDelete }: QueriesTableProps) {
    // Manage sorting state for the table
    const { sortedData: sortedQueries, sortDescriptor, setSortDescriptor } = useSortableData(queries, {
        column: 'label',
        direction: 'ascending',
    });

    return (
        <SortedQueriesTable
            queries={sortedQueries}
            onDelete={onDelete}
            sortDescriptor={sortDescriptor}
            onSortChange={setSortDescriptor}
        />
    );
}

type SortedQueriesTableProps = {
    queries: Query[];
    onDelete: (id: Id) => void;
    sortDescriptor: SortDescriptor;
    onSortChange: (sortDescriptor: SortDescriptor) => void;
};

function SortedQueriesTable({ queries, onDelete, sortDescriptor, onSortChange }: SortedQueriesTableProps) {
    const [ toDelete, setToDelete ] = useState<Query>();
    const { showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();
    const navigate = useNavigate();

    function navigateToQuery(key: React.Key) {
        const queryId = key as Id;
        const path = routes.category.queries.detail.resolve({ categoryId: category.id!, queryId });
        navigate(path, { state: { sortDescriptor } });
    }

    return (<>
        <DeleteQueryModal
            query={toDelete}
            onClose={() => setToDelete(undefined)}
            onDelete={onDelete}
        />

        <Table
            aria-label='Query Table'
            onRowAction={navigateToQuery}
            sortDescriptor={sortDescriptor}
            onSortChange={onSortChange}
        >
            <TableHeader>
                {[
                    ...(showTableIDs ? [
                        <TableColumn key='id' allowsSorting>ID</TableColumn>,
                    ] : []),
                    <TableColumn key='label' allowsSorting>Label</TableColumn>,
                    <TableColumn key='version' allowsSorting>Version</TableColumn>,
                    <TableColumn key='accessPath'>Content</TableColumn>,
                    <TableColumn key='actions'>Actions</TableColumn>,
                ]}
            </TableHeader>

            <TableBody emptyContent='No rows to display.'>
                {queries.map(query => (
                    <TableRow key={query.id} className='cursor-pointer hover:bg-default-100 focus:bg-default-200'>
                        {[
                            ...(showTableIDs ? [
                                <TableCell key='id'>{query.id}</TableCell>,
                            ] : []),
                            <TableCell key='label'>{query.label}</TableCell>,
                            <TableCell key='version'>
                                <span className={Number(category.systemVersionId) > Number(query.version) ? 'text-danger-500' : ''}>
                                    {query.version}
                                </span>
                            </TableCell>,
                            <TableCell key='accessPath'>
                                <QueryContentTooltip query={query} text='Preview' />
                            </TableCell>,
                            <TableCell key='actions' title='Delete query'>
                                <Button
                                    isIconOnly
                                    aria-label='Delete'
                                    color='danger'
                                    variant='light'
                                    onPress={() => setToDelete(query)}
                                    title='Delete query'
                                >
                                    <TrashIcon className='size-5' />
                                </Button>
                            </TableCell>,
                        ]}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    </>);
}

type DeleteQueryModalProps = {
    query: Query | undefined;
    onClose: () => void;
    onDelete: (id: Id) => void;
};

function DeleteQueryModal({ query, onClose, onDelete }: DeleteQueryModalProps) {
    const [ isFetching, setIsFetching ] = useState<boolean>(false);

    const cached = useCached(query);

    async function confirmDelete() {
        if (!query)
            return;

        setIsFetching(true);
        const response = await api.queries.deleteQuery({ queryId: query.id });
        setIsFetching(false);
        if (!response.status) {
            toast.error('Failed to delete query.');
            return;
        }

        toast.success(`Query ${query.label} deleted successfully!`);
        onDelete(query.id);
        onClose();
    }

    return (
        <ConfirmationModal
            isOpen={!!query}
            onClose={onClose}
            onConfirm={confirmDelete}
            isFetching={isFetching}
            title='Confirm Deletion?'
            message={`This will permanently delete the query ${cached?.label ?? ''}.`}
            confirmButtonText='Yes, Delete'
            cancelButtonText='Cancel'
            confirmButtonColor='danger'
        />
    );
}

function QueryContentTooltip({ query, text }: { query: Query, text: string }) {
    return (
        <Tooltip
            content={
                <pre className='text-sm p-2'>
                    {query.content}
                </pre>
            }
        >
            <span className='underline cursor-pointer'>
                {text}
            </span>
        </Tooltip>
    );
}
