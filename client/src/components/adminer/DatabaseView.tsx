import { useState, useEffect, useMemo, useCallback } from 'react';
import { Spinner, Pagination } from '@nextui-org/react';
import { FilterForm } from '@/components/adminer/FilterForm';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { View } from '@/types/adminer/View';
import { api } from '@/api';
import { useFetchReferences } from './useFetchReferences';
import { useFetchData } from './useFetchData';
import type { Id } from '@/types/id';
import type { AdminerState, AdminerStateAction, KindFilterState } from '@/types/adminer/Reducer';
import type { FetchKindParams } from '@/types/adminer/FetchParams';
import type { GraphResponse, DataResponse, TableResponse, DocumentResponse } from '@/types/adminer/DataResponse';
import type { DatasourceType } from '@/types/datasource/Datasource';

function getUrlParams(offset: number, active: KindFilterState, datasourceId?: Id, kindName?: string) {
    const filterExist = active.filters?.some(filter => {
        return filter.propertyName.length > 0 && filter.operator && filter.propertyValue.length > 0;
    });

    const urlParams: FetchKindParams = { datasourceId: datasourceId!, kindId: kindName!, queryParams: { limit: active.limit, offset: offset } };

    if (active.filters && filterExist) {
        const queryFilters = `${active.filters
            .map(
                filter =>
                    filter.propertyName.length > 0 && filter.operator && filter.propertyValue.length > 0 ? `(${filter.propertyName},${filter.operator},${filter.propertyValue})` : '',
            )
            .join('')}`;
        urlParams.queryParams.filters = queryFilters;
    }

    return urlParams;
}

type DatabaseViewProps = Readonly<{
    state: AdminerState;
    datasourceType: DatasourceType;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function DatabaseView({ state, datasourceType, dispatch }: DatabaseViewProps) {
    const { references, refLoading, refError } = useFetchReferences(state);

    const [ currentPage, setCurrentPage ] = useState(1);
    const [ offset, setOffset ] = useState(0);
    const [ itemCount, setItemCount ] = useState<number>();
    const [ totalPages, setTotalPages ] = useState(1);

    useEffect(() => {
        if (itemCount)
            setTotalPages(Math.ceil(itemCount / state.active.limit));

        if (currentPage > totalPages) {
            setCurrentPage(totalPages);
            setOffset(state.active.limit * (totalPages - 1));
        }
    }, [ itemCount, offset, currentPage, totalPages, state.active.limit ]);

    const urlParams = useMemo(() => {
        return getUrlParams(offset, state.active, state.datasourceId, state.kindName);
    }, [ state.active, state.datasourceId, state.kindName, offset ]);

    const fetchFunction = useCallback(() => {
        return api.adminer.getKind({ datasourceId: urlParams.datasourceId, kindId: urlParams.kindId }, urlParams.queryParams);
    }, [ urlParams ]);

    const { fetchedData, loading, error } = useFetchData<DataResponse>(fetchFunction);

    useEffect(() => {
        setItemCount(undefined);
        setTotalPages(1);
        setCurrentPage(1);
        setOffset(0);
    }, [ state.active, state.datasourceId, state.kindName, state.view ]);

    if (!fetchedData || loading || refLoading) {
        return (
            <div className='h-10 flex items-center justify-center'>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;
    if (refError)
        return <p>{refError}</p>;

    return (
        <div>
            <div className='mb-5'>
                <FilterForm state={state} datasourceType={datasourceType} propertyNames={fetchedData?.metadata.propertyNames} dispatch={dispatch}/>
            </div>

            {state.view === View.table ? (
                <DatabaseTable fetchedData={fetchedData as TableResponse | GraphResponse} setItemCount={setItemCount} references={references}/>
            ) : (
                <DatabaseDocument fetchedData={fetchedData as DocumentResponse | GraphResponse} setItemCount={setItemCount} references={references}/>
            )}

            {itemCount !== undefined && itemCount > 0 && (
                <div className='mt-5 inline-flex gap-3 items-center'>
                    <Pagination
                        total={totalPages}
                        page={currentPage}
                        onChange={page => {
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
