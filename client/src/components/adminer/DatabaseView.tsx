import { useState } from 'react';
import { Pagination } from '@nextui-org/react';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { Operator } from '@/types/adminer/ColumnFilter';
import { View } from '@/types/adminer/View';
import { type AdminerState } from '@/types/adminer/Reducer';

type DatabaseViewProps = Readonly<{
    apiUrl: string;
    state: AdminerState;
}>;

function generateUrl(apiUrl: string, state: AdminerState, offset: number) {
    const filterExist = state.active.filters?.some((filter) => {
        return filter.columnName.length > 0 && filter.operator && filter.columnValue.length > 0;
    });

    if (state.active.filters && filterExist) {
        return `${apiUrl}/${state.datasource!.id}/${state.kind}?filters=${state.active.filters
            .map(
                (filter) =>
                    filter.columnName.length > 0 && filter.operator && filter.columnValue.length > 0 ? `(${filter.columnName},${Operator[filter.operator as keyof typeof Operator]},${filter.columnValue})` : '',
            )
            .join('')}&limit=${state.active.limit}&offset=${offset}`;
    }

    return `${apiUrl}/${state.datasource!.id}/${state.kind}?limit=${state.active.limit}&offset=${offset}`;
}

export function DatabaseView({ apiUrl, state }: DatabaseViewProps) {
    const [ paginationState, setPaginationState ] = useState({
        currentPage: 1,
        offset: 0,
        rowCount: undefined as number | undefined,
        totalPages: 1,
    });

    const { currentPage, offset, rowCount, totalPages } = paginationState;

    const url = generateUrl(apiUrl, state, offset);

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
                <DatabaseTable apiUrl={url} setRowCount={updatePaginationState}/>
            ) : (
                <DatabaseDocument apiUrl={url} setRowCount={updatePaginationState}/>
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
