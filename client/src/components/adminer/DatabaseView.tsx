import { useState, useMemo } from 'react';
import { Pagination } from '@nextui-org/react';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { Operator } from '@/types/adminer/ColumnFilter';
import { View } from '@/types/adminer/View';
import { type AdminerState } from '@/types/adminer/Reducer';
import { type FetchKindParams } from '@/types/adminer/FetchParams';

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
    const [ paginationState, setPaginationState ] = useState({
        currentPage: 1,
        offset: 0,
        rowCount: undefined as number | undefined,
        totalPages: 1,
    });

    const { currentPage, offset, rowCount, totalPages } = paginationState;

    const urlParams = useMemo(() => getUrlParams(state, offset), [ state.datasource, state.kind, state.view , state.active, offset ]);

    const updatePaginationState = (newRowCount?: number) => {
        setPaginationState((prev) => {
            const newRow = newRowCount ?? prev.rowCount;
            const calculatedTotalPages = newRow ? Math.ceil(newRow / state.active.limit) : 1;
            const newPage = Math.min(prev.currentPage, calculatedTotalPages);
            const newOffset = state.active.limit * (newPage - 1);

            return {
                ...prev,
                rowCount: newRow,
                totalPages: calculatedTotalPages,
                currentPage: newPage,
                offset: newOffset,
            };
        });
    };

    return (
        <div className='mt-5'>
            {state.view === View.table ? (
                <DatabaseTable urlParams={urlParams} setRowCount={updatePaginationState}/>
            ) : (
                <DatabaseDocument urlParams={urlParams} setRowCount={updatePaginationState}/>
            )}

            <div className='mt-5 inline-flex gap-3 items-center'>
                <Pagination
                    total={totalPages}
                    page={currentPage}
                    onChange={(page) => {
                        setPaginationState((prev) => ({
                            ...prev,
                            currentPage: page,
                            offset: state.active.limit * (page - 1),
                        }));
                    }}
                    color='primary'
                />
                {rowCount != undefined && rowCount > 0 && (
                    <p>Number of rows: {rowCount}</p>
                )}
            </div>

        </div>
    );
}
