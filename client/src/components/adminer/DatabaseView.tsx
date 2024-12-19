import { useState, useEffect, useMemo, useCallback } from 'react';
import { Spinner, Pagination } from '@nextui-org/react';
import { FilterForm } from '@/components/adminer/FilterForm';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { Operator } from '@/types/adminer/PropertyFilter';
import { View } from '@/types/adminer/View';
import { api } from '@/api';
import { useFetchReferences } from './useFetchReferences';
import { useFetchData } from './useFetchData';
import type { AdminerState, AdminerStateAction } from '@/types/adminer/Reducer';
import type { FetchKindParams } from '@/types/adminer/FetchParams';

function getUrlParams(state: AdminerState, offset: number) {
    const filterExist = state.active.filters?.some((filter) => {
        return filter.propertyName.length > 0 && filter.operator && filter.propertyValue.length > 0;
    });

    const urlParams: FetchKindParams = { datasourceId: state.datasourceId!, kindId: state.kindName!, queryParams: { limit: state.active.limit, offset: offset } };

    if (state.active.filters && filterExist) {
        const queryFilters = `${state.active.filters
            .map(
                (filter) =>
                    filter.propertyName.length > 0 && filter.operator && filter.propertyValue.length > 0 ? `(${filter.propertyName},${Operator[filter.operator as unknown as keyof typeof Operator]},${filter.propertyValue})` : '',
            )
            .join('')}`;
        urlParams.queryParams.filters = queryFilters;
    }

    return urlParams;
}

type DatabaseViewProps = Readonly<{
    state: AdminerState;
    dispatch: React.Dispatch<AdminerStateAction>;
}>;

export function DatabaseView({ state, dispatch }: DatabaseViewProps) {
    const { references, refLoading, refError } = useFetchReferences(state);

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

    const fetchFunction = useCallback(() => {
        return api.adminer.getKind({ datasourceId: urlParams.datasourceId, kindId: urlParams.kindId }, urlParams.queryParams);
    }, [ urlParams ]);

    const { fetchedData, loading, error } = useFetchData(fetchFunction);

    useEffect(() => {
        setItemCount(undefined);
        setTotalPages(1);
        setCurrentPage(1);
        setOffset(0);
    }, [ state.active, state.datasourceId, state.kindName, state.view ]);

    if (loading || refLoading) {
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
        <div className='mt-5'>
            <div className='mt-5'>
                <FilterForm state={state} dispatch={dispatch} propertyNames={fetchedData?.metadata.propertyNames}/>
            </div>

            {state.view === View.table ? (
                <DatabaseTable fetchedData={fetchedData} setItemCount={setItemCount} references={references}/>
            ) : (
                <DatabaseDocument fetchedData={fetchedData} setItemCount={setItemCount} references={references}/>
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
