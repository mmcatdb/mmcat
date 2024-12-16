import { useState, useEffect, useMemo } from 'react';
import { Spinner, Pagination } from '@nextui-org/react';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { Operator } from '@/types/adminer/ColumnFilter';
import { View } from '@/types/adminer/View';
import type { AdminerState } from '@/types/adminer/Reducer';
import type { FetchKindParams } from '@/types/adminer/FetchParams';
import { useFetchReferences } from './useFetchReferences';

type DatabaseViewProps = Readonly<{
    state: AdminerState;
}>;

function getUrlParams(state: AdminerState, offset: number) {
    const filterExist = state.active.filters?.some((filter) => {
        return filter.columnName.length > 0 && filter.operator && filter.columnValue.length > 0;
    });

    const urlParams: FetchKindParams = { datasourceId: state.datasourceId!, kindId: state.kindName!, queryParams: { limit: state.active.limit, offset: offset } };

    if (state.active.filters && filterExist) {
        const queryFilters = `${state.active.filters
            .map(
                (filter) =>
                    filter.columnName.length > 0 && filter.operator && filter.columnValue.length > 0 ? `(${filter.columnName},${Operator[filter.operator as unknown as keyof typeof Operator]},${filter.columnValue})` : '',
            )
            .join('')}`;
        urlParams.queryParams.filters = queryFilters;
    }

    return urlParams;
}

export function DatabaseView({ state }: DatabaseViewProps) {
    const { references, loading, error } = useFetchReferences(state);

    const [ currentPage, setCurrentPage ] = useState(1);
    const [ offset, setOffset ] = useState<number>(0);
    const [ itemCount, setItemCount ] = useState<number | undefined>();
    const [ totalPages, setTotalPages ] = useState<number>(1);

    useEffect(() => {
        if (itemCount)
            setTotalPages(Math.ceil(itemCount / state.active.limit));

        if (currentPage > totalPages) {
            setCurrentPage(totalPages);
            setOffset(state.active.limit * (totalPages - 1));
        }
    }, [ itemCount, offset, currentPage, totalPages, state.active.limit ]);

    const urlParams = useMemo(() => {
        return getUrlParams(state, offset);
    }, [ state.active, state.datasourceId, state.kindName, state.view, offset ]);

    useEffect(() => {
        setItemCount(undefined);
        setTotalPages(1);
        setCurrentPage(1);
        setOffset(0);
    }, [ state.active, state.datasourceId, state.kindName, state.view ]);

    if (loading) {
        return (
            <div className='h-10 flex items-center justify-center'>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;

    return (
        <div className='mt-5'>
            {state.view === View.table ? (
                <DatabaseTable urlParams={urlParams} setItemCount={setItemCount} references={references}/>
            ) : (
                <DatabaseDocument urlParams={urlParams} setItemCount={setItemCount} references={references}/>
            )}

            {itemCount !== undefined && itemCount > 0 && (
                <div className='mt-5 inline-flex gap-3 items-center'>
                    <Pagination
                        total={totalPages}
                        page={currentPage}
                        onChange={(page) => {
                            setCurrentPage(page);
                            setOffset(state.active.limit * (page - 1));
                        }}
                        color='primary'
                    />
                    <p>Number of rows: {itemCount}</p>
                </div>
            )}
        </div>
    );
}
