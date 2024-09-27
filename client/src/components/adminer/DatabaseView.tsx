import { useState, useEffect } from 'react';
import { Pagination } from '@nextui-org/react';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseList } from '@/components/adminer/DatabaseList';
import { DatasourceType } from '@/types/datasource';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';

type DatabaseViewProps = Readonly<{
    apiUrl: string;
    filters: ColumnFilter[] | undefined;
    datasourceType: DatasourceType;
    limit: number;
}>;

export function DatabaseView({ apiUrl, filters, datasourceType, limit }: DatabaseViewProps) {
    const [ currentPage, setCurrentPage ] = useState(1);
    const [ offset, setOffset ] = useState<number>(0);
    const [ rowCount, setRowCount ] = useState<number | undefined>();
    const [ totalPages, setTotalPages ] = useState<number>(1);
    const [ url, setUrl ] = useState<string>(filters ? `${apiUrl}?filters=${filters
        .map(filter => `(${filter.columnName},${Operator[filter.operator as keyof typeof Operator]},${filter.columnValue})`)
        .join('')}&limit=${limit}&offset=${offset}` : `${apiUrl}?limit=${limit}&offset=${offset}`);

    useEffect(() => {
        if (rowCount)
            setTotalPages(Math.ceil(rowCount / limit));

        if (currentPage > totalPages) {
            setCurrentPage(totalPages);
            setOffset(limit * (totalPages - 1));
        }

        setUrl( filters ? `${apiUrl}?filters=${filters
            .map(filter => `(${filter.columnName},${Operator[filter.operator as keyof typeof Operator]},${filter.columnValue})`)
            .join('')}&limit=${limit}&offset=${offset}` : `${apiUrl}?limit=${limit}&offset=${offset}`);
    }, [ rowCount, limit, offset, currentPage, totalPages, apiUrl, filters ]);

    return (
        <div className='mt-5'>
            {datasourceType === DatasourceType.postgresql ? (
                <DatabaseTable apiUrl={url} setRowCount={setRowCount}/>
            ) : (
                <DatabaseList apiUrl={url} setRowCount={setRowCount}/>
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
        </div>
    );
}
