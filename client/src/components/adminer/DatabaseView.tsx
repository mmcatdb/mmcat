import { useState, useEffect } from 'react';
import { Pagination } from '@nextui-org/react';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { type DatasourceType } from '@/types/datasource';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';
import { type AdminerFilterState } from '@/types/adminer/Reducer';
import { View } from '@/types/adminer/View';

type DatabaseViewProps = Readonly<{
    apiUrl: string;
    datasourceId: string;
    tableName: string;
    datasourceType: DatasourceType;
    state: AdminerFilterState;
    view: View;
}>;

function generateUrl(apiUrl: string, datasourceId: string, tableName: string, filters: ColumnFilter[] | undefined, limit: number, offset: number) {
    const filterExist = filters?.some((filter) => {
        return filter.columnName.length > 0 && filter.operator && filter.columnValue.length > 0;
    });

    if (filters && filterExist) {
        return `${apiUrl}/${datasourceId}/${tableName}?filters=${filters
            .map(
                (filter) =>
                    filter.columnName.length > 0 && filter.operator && filter.columnValue.length > 0 ? `(${filter.columnName},${Operator[filter.operator as keyof typeof Operator]},${filter.columnValue})` : '',
            )
            .join('')}&limit=${limit}&offset=${offset}`;
    }

    return `${apiUrl}/${datasourceId}/${tableName}?limit=${limit}&offset=${offset}`;
}

export function DatabaseView({ apiUrl, datasourceId, tableName, datasourceType, state, view }: DatabaseViewProps) {
    const [ currentPage, setCurrentPage ] = useState(1);
    const [ offset, setOffset ] = useState<number>(0);
    const [ rowCount, setRowCount ] = useState<number | undefined>();
    const [ totalPages, setTotalPages ] = useState<number>(1);
    const [ url, setUrl ] = useState<string>(generateUrl(apiUrl, datasourceId, tableName, state.submitted.filters, state.submitted.limit, offset));

    useEffect(() => {
        if (rowCount)
            setTotalPages(Math.ceil(rowCount / state.submitted.limit));

        if (currentPage > totalPages) {
            setCurrentPage(totalPages);
            setOffset(state.submitted.limit * (totalPages - 1));
        }
    }, [ rowCount, state, offset, currentPage, totalPages ]);

    useEffect(() => {
        setUrl(generateUrl(apiUrl, datasourceId, tableName, state.submitted.filters, state.submitted.limit, offset));
    }, [ apiUrl, state, offset, datasourceId, tableName ]);

    useEffect(() => {
        setRowCount(undefined);
        setTotalPages(1);
        setCurrentPage(1);
        setOffset(0);
    }, [ datasourceType, apiUrl  ]);

    return (
        <div className='mt-5'>
            {view === View.table ? (
                <DatabaseTable apiUrl={url} setRowCount={setRowCount}/>
            ) : (
                <DatabaseDocument apiUrl={url} setRowCount={setRowCount}/>
            )}

            <div className='mt-5 inline-flex gap-3 items-center'>
                <Pagination
                    total={totalPages}
                    page={currentPage}
                    onChange={(page) => {
                        setCurrentPage(page);
                        setOffset(state.submitted.limit * (page - 1));
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
