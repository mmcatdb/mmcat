import { useState, useEffect } from 'react';
import { Pagination } from '@nextui-org/react';
import { useFetchTableData } from './useFetchTableData';
import { useFetchDocumentData } from './useFetchDocumentData';
import { useFetchGraphData } from './useFetchGraphData';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { DatasourceType } from '@/types/datasource';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';
import { View } from '@/types/adminer/View';

type DatabaseViewProps = Readonly<{
    apiUrl: string;
    filters: ColumnFilter[] | undefined;
    datasourceType: DatasourceType;
    limit: number;
    view: View;
}>;

function generateUrl(apiUrl: string, filters: ColumnFilter[] | undefined, limit: number, offset: number) {
    if (filters) {
        return `${apiUrl}?filters=${filters
            .map(
                (filter) =>
                    `(${filter.columnName},${Operator[filter.operator as keyof typeof Operator]},${filter.columnValue})`,
            )
            .join('')}&limit=${limit}&offset=${offset}`;
    }

    return `${apiUrl}?limit=${limit}&offset=${offset}`;
}

function getFetchFunction(datasourceType: DatasourceType) {
    switch (datasourceType) {
    case DatasourceType.postgresql:
        return useFetchTableData;
    case DatasourceType.mongodb:
        return useFetchDocumentData;
    case DatasourceType.neo4j:
        return useFetchGraphData;
    default:
        throw new Error('Invalid datasource type');
    }
}

export function DatabaseView({ apiUrl, filters, datasourceType, limit, view }: DatabaseViewProps) {
    const [ currentPage, setCurrentPage ] = useState(1);
    const [ offset, setOffset ] = useState<number>(0);
    const [ rowCount, setRowCount ] = useState<number | undefined>();
    const [ totalPages, setTotalPages ] = useState<number>(1);
    const [ url, setUrl ] = useState<string>(generateUrl(apiUrl, filters, limit, offset));

    const fetchData = getFetchFunction(datasourceType);

    useEffect(() => {
        if (rowCount)
            setTotalPages(Math.ceil(rowCount / limit));

        if (currentPage > totalPages) {
            setCurrentPage(totalPages);
            setOffset(limit * (totalPages - 1));
        }
    }, [ rowCount, limit, offset, currentPage, totalPages ]);

    useEffect(() => {
        setUrl(generateUrl(apiUrl, filters, limit, offset));
    }, [ filters, apiUrl, limit, offset ]);

    return (
        <div className='mt-5'>
            {view === View.table ? (
                <DatabaseTable apiUrl={url} fetchData={fetchData} setRowCount={setRowCount}/>
            ) : (
                <DatabaseDocument apiUrl={url} fetchData={fetchData} setRowCount={setRowCount}/>
            )}
            <Pagination
                className='mt-5'
                total={totalPages}
                page={currentPage}
                onChange={(page) => {
                    setCurrentPage(page);
                    setOffset(limit * (page - 1));
                }}
                color='primary'
            />
            <p className='mt-5'>Number of rows: {rowCount}</p>
        </div>
    );
}
