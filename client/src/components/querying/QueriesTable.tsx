import { type Dispatch, useMemo, useState } from 'react';
import { Table, TableHeader, TableBody, TableColumn, TableRow, TableCell, Button, Tooltip, Pagination } from '@heroui/react';
import { EyeIcon, TrashIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { type Query } from '@/types/query';
import { useNavigate } from 'react-router-dom';
import { usePreferences } from '../context/PreferencesProvider';
import { ConfirmationModal } from '../common/tableComponents';
import { type Comparator, REMOVED_SORT_KEY, usePagination, useSortable } from '../common/tableUtils';
import { routes } from '@/routes/routes';
import { toast } from 'react-toastify';
import { compareVersionIdsAsc, type Id } from '@/types/id';
import { useCached } from '../hooks/useCached';
import { api } from '@/api';
import { useCategoryInfo } from '../context/CategoryInfoProvider';
import { QueryWeightTableDisplay } from './QueryOutputDisplay';
import { type AdaptationSolution } from '../adaptation/adaptation';
import { prettyPrintDouble } from '@/types/utils/common';

type QueriesTableProps = {
    queries: Query[];
    onUpdate?: Dispatch<Query>;
    onDelete?: Dispatch<Id>;
    itemsPerPage?: number;
    solution?: AdaptationSolution;
};

/**
 * Renders a sortable table of queries with delete functionality.
 */
export function QueriesTable({ queries, onUpdate, onDelete, itemsPerPage, solution }: QueriesTableProps) {
    const { showTableIDs } = usePreferences().preferences;
    const { category } = useCategoryInfo();

    const [ toDelete, setToDelete ] = useState<Query>();

    const navigate = useNavigate();

    const { finalQueries, allWeights } = useMemo(() => {
        const finalQueries = solution ? [ ...solution.queries.values().map(q => q.query) ] : queries;
        return {
            finalQueries,
            allWeights: finalQueries.reduce((ans, query) => ans + query.finalWeight, 0),
        };
    }, [ queries, solution ]);

    const queryComparator = useMemo(() => createQueryComparator(solution), [ solution ]);
    const { sorted, sortDescriptor, setSortDescriptor } = useSortable(finalQueries, {
        column: 'label',
        direction: 'ascending',
    }, queryComparator);

    const { paginated, paginator } = usePagination(sorted, itemsPerPage);

    function navigateToQuery(key: React.Key) {
        const queryId = key as Id;
        const path = routes.category.queries.detail.resolve({ categoryId: category.id!, queryId });
        navigate(path, { state: { sortDescriptor } });
    }

    return (<>
        {onDelete && (
            <DeleteQueryModal
                query={toDelete}
                onClose={() => setToDelete(undefined)}
                onDelete={onDelete}
            />
        )}

        <Table
            aria-label='Query Table'
            onRowAction={onUpdate ? undefined : navigateToQuery}
            sortDescriptor={sortDescriptor}
            onSortChange={setSortDescriptor}
            bottomContent={paginator && (
                <div className='flex w-full justify-end'>
                    <Pagination showControls variant='light' className='[&_[role=button]]:cursor-pointer' {...paginator} />
                </div>
            )}
        >
            <TableHeader>
                {[
                    ...(showTableIDs ? [
                        <TableColumn key='id' allowsSorting>ID</TableColumn>,
                    ] : []),
                    <TableColumn key='label' allowsSorting>Label</TableColumn>,
                    <TableColumn key='version' allowsSorting align='end'>Version</TableColumn>,
                    <TableColumn key='weight' allowsSorting align='end'>Weight (<span className='italic'>normalized</span>)</TableColumn>,
                    ...(solution ? [
                        <TableColumn key='speedup' allowsSorting align='end'>Speed up [<XMarkIcon className='inline size-4' />]</TableColumn>,
                    ] : []),
                    <TableColumn key='actions' minWidth={88} align='end'><span className='mr-4'>Actions</span></TableColumn>,
                ]}
            </TableHeader>

            <TableBody emptyContent='No rows to display.'>
                {paginated.map(query => (
                    // The `map` is necessary to make the table actually re-render when the queries change.
                    // Because when the solution changes, the queries objects remain the same, so the table wouldn't re-render (if we used `items={paginated}`).
                    <TableRow key={query.id} className={onUpdate ? undefined : 'cursor-pointer hover:bg-default-100 focus:bg-default-200'}>
                        {[
                            ...(showTableIDs ? [
                                <TableCell key='id'>{query.id}</TableCell>,
                            ] : []),
                            <TableCell key='label'>{query.label}</TableCell>,
                            <TableCell key='version' className={Number(category.systemVersionId) > Number(query.version) ? 'text-danger-500' : ''}>
                                {query.version}
                            </TableCell>,
                            <TableCell key='weight'>
                                <QueryWeightTableDisplay query={query} allWeights={allWeights} onUpdate={onUpdate} />
                            </TableCell>,
                            ...(solution ? [
                                <TableCell key='speedup'>
                                    {prettyPrintDouble(solution.queries.get(query.id)?.speedup ?? 1)}
                                </TableCell>,
                            ] : []),
                            <TableCell key='actions' className='p-0'>
                                <div className='flex justify-end gap-2'>
                                    <QueryContentTooltip query={query} />

                                    {onDelete && (
                                        <Button
                                            isIconOnly
                                            color='danger'
                                            variant='light'
                                            onPress={() => setToDelete(query)}
                                            aria-label='Delete query'
                                        >
                                            <TrashIcon className='size-5' />
                                        </Button>
                                    )}
                                </div>
                            </TableCell>,
                        ]}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    </>);
}

function createQueryComparator(solution: AdaptationSolution | undefined): Comparator<Query> {
    return {
        version: (a, b) => compareVersionIdsAsc(a.version, b.version),
        weight: (a, b) => a.finalWeight - b.finalWeight,
        speedup: !solution ? REMOVED_SORT_KEY : (a, b) => {
            const speedupA = solution.queries.get(a.id)?.speedup ?? 1;
            const speedupB = solution.queries.get(b.id)?.speedup ?? 1;
            return speedupA - speedupB;
        },
    };
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

function QueryContentTooltip({ query }: { query: Query }) {
    return (
        <Tooltip
            content={
                <pre className='text-sm p-2'>
                    {query.content}
                </pre>
            }
        >
            <Button isIconOnly variant='light' aria-label='Preview query content'>
                <EyeIcon className='size-5' />
            </Button>
        </Tooltip>
    );
}
