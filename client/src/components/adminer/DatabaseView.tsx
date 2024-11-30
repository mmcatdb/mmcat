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

    const urlParams: FetchKindParams = { datasourceId: state.datasource!.id, kindId: state.kind!, queryParams: { limit: state.active.limit, offset: offset } };

    if (state.active.filters && filterExist) {
        const queryFilters = `${state.active.filters
            .map(
                (filter) =>
                    filter.columnName.length > 0 && filter.operator && filter.columnValue.length > 0 ? `(${filter.columnName},${Operator[filter.operator as keyof typeof Operator]},${filter.columnValue})` : '',
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
    const [ rowCount, setRowCount ] = useState<number | undefined>();
    const [ totalPages, setTotalPages ] = useState<number>(1);

    useEffect(() => {
        if (rowCount)
            setTotalPages(Math.ceil(rowCount / state.active.limit));

        if (currentPage > totalPages) {
            setCurrentPage(totalPages);
            setOffset(state.active.limit * (totalPages - 1));
        }
    }, [ rowCount, offset, currentPage, totalPages, state.active.limit ]);

    const urlParams = useMemo(() => {
        return getUrlParams(state, offset);
    }, [ state.active, state.datasource, state.kind, state.view, offset ]);

    useEffect(() => {
        setRowCount(undefined);
        setTotalPages(1);
        setCurrentPage(1);
        setOffset(0);
    }, [ state.active, state.datasource, state.kind, state.view ]);

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
                <DatabaseTable urlParams={urlParams} setRowCount={setRowCount} references={references}/>
            ) : (
                <DatabaseDocument urlParams={urlParams} setRowCount={setRowCount} references={references}/>
            )}

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
                {rowCount && (
                    <p>Number of rows: {rowCount}</p>
                )}
            </div>

        </div>
    );
}
