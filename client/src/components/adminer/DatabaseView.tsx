import { useState, useEffect, useCallback } from 'react';
import { Spinner, Pagination } from '@nextui-org/react';
import { ExportComponent } from '@/components/adminer//ExportComponent';
import { FilterForm } from '@/components/adminer/FilterForm';
import { DatabaseTable } from '@/components/adminer/DatabaseTable';
import { DatabaseDocument } from '@/components/adminer/DatabaseDocument';
import { DatabaseGraph } from '@/components/adminer/DatabaseGraph';
import { View } from '@/types/adminer/View';
import { api } from '@/api';
import { useFetchReferences } from './useFetchReferences';
import { useFetchData } from './useFetchData';
import { getFiltersURLParam } from './URLParamsState';
import type { Id } from '@/types/id';
import type { QueryParams } from '@/types/api/routes';
import type { AdminerFilterQueryState, AdminerFilterQueryStateAction, KindFilterState } from '@/types/adminer/ReducerTypes';
import type { GraphResponse, DataResponse, TableResponse, DocumentResponse } from '@/types/adminer/DataResponse';
import type { AdminerReferences, KindReference } from '@/types/adminer/AdminerReferences';
import type { Datasource } from '@/types/datasource/Datasource';

function getQueryParams(filterState: KindFilterState): QueryParams {
    if (filterState.filters.length > 0)
        return { filters: getFiltersURLParam(filterState), limit: filterState.limit, offset: filterState.offset };

    return { limit: filterState.limit, offset: filterState.offset };
}

function getKindReferences(references: AdminerReferences, datasourceId: Id, kind: string) {
    const outgoingReferences: KindReference[] = references
        ? Object.values(references)
            .filter(ref => ref.referencingDatasourceId === datasourceId && ref.referencingKindName === kind)
            .map(ref => ({
                referencingProperty: ref.referencingProperty,
                datasourceId: ref.referencedDatasourceId,
                kindName: ref.referencedKindName,
                property: ref.referencedProperty,
            }))
        : [];
    const incomingReferences: KindReference[] = references
        ? Object.values(references)
            .filter(ref => ref.referencedDatasourceId === datasourceId && ref.referencedKindName === kind)
            .map(ref => ({
                referencingProperty: ref.referencedProperty,
                datasourceId: ref.referencingDatasourceId,
                kindName: ref.referencingKindName,
                property: ref.referencingProperty,
            }))
        : [];

    return [ ...incomingReferences, ...outgoingReferences ];
}

type DatabaseViewProps = Readonly<{
    state: AdminerFilterQueryState;
    datasources: Datasource[];
    dispatch: React.Dispatch<AdminerFilterQueryStateAction>;
}>;

export function DatabaseView({ state, datasources, dispatch }: DatabaseViewProps) {
    const { references, referencesLoading } = useFetchReferences(state);

    const [ currentPage, setCurrentPage ] = useState(1);
    const [ offset, setOffset ] = useState(0);
    const [ itemCount, setItemCount ] = useState<number>();
    const [ totalPages, setTotalPages ] = useState(1);

    useEffect(() => {
        dispatch({ type: 'input', field: 'offset', value: offset });
    }, [ offset, dispatch ]);

    useEffect(() => {
        if (itemCount)
            setTotalPages(Math.ceil(itemCount / state.active.limit));

        if (currentPage > totalPages) {
            setCurrentPage(totalPages);
            setOffset(state.active.limit * (totalPages - 1));
        }
    }, [ itemCount, offset, currentPage, totalPages, state.active.limit ]);

    const fetchFunction = useCallback(() => {
        return api.adminer.getKind({ datasourceId: state.datasourceId!, kindName: state.kindName! }, getQueryParams(state.active));
    }, [ state.datasourceId, state.kindName, state.active ]);

    const { fetchedData, loading, error } = useFetchData<DataResponse>(fetchFunction);

    useEffect(() => {
        setItemCount(undefined);
        setTotalPages(1);
        setCurrentPage(1);
        setOffset(0);
    }, [ state.datasourceId, state.kindName ]);

    useEffect(() => {
        const count = fetchedData?.metadata.itemCount;
        count ? setItemCount(count) : setItemCount(0);
    }, [ fetchedData ]);

    if (error === `Failed to fetch data`) {
        return (
            <>
                <FilterForm state={state} datasourceType={datasources.find(source => source.id === state.datasourceId)!.type} propertyNames={undefined} dispatch={dispatch}/>

                <p>{error}</p>
            </>

        );
    }

    if (loading || referencesLoading) {
        return (
            <div className='h-10 flex items-center justify-center'>
                <Spinner />
            </div>
        );
    }

    if (error)
        return <p>{error}</p>;

    const kindReferences: KindReference[] = getKindReferences(references, state.datasourceId!, state.kindName!);

    return (
        <>
            <FilterForm state={state} datasourceType={datasources.find(source => source.id === state.datasourceId)!.type} propertyNames={fetchedData?.metadata.propertyNames} dispatch={dispatch}/>

            {itemCount !== undefined && itemCount > 0 && (
                <div className='my-1 inline-flex gap-2 items-center'>
                    {state.view !== View.graph && (
                        <>
                            <Pagination
                                size='sm'
                                total={totalPages}
                                page={currentPage}
                                onChange={page => {
                                    setCurrentPage(page);
                                    setOffset(state.active.limit * (page - 1));
                                }}
                                color='primary'
                            />
                            <p>Number of rows: {itemCount}</p>
                        </>
                    )}

                    <ExportComponent data={fetchedData}/>
                </div>
            )}

            <div className='flex'>
                {(() => {
                    switch (state.view) {
                    case View.table:
                        return (
                            <DatabaseTable
                                fetchedData={fetchedData as TableResponse | GraphResponse}
                                kindReferences={kindReferences}
                                kind={state.kindName!}
                                datasourceId={state.datasourceId!}
                                datasources={datasources}
                            />
                        );
                    case View.document:
                        return (
                            <DatabaseDocument
                                fetchedData={fetchedData as DocumentResponse | GraphResponse}
                                kindReferences={kindReferences}
                                kind={state.kindName!}
                                datasourceId={state.datasourceId!}
                                datasources={datasources}
                            />
                        );
                    case View.graph:
                        return (
                            <DatabaseGraph
                                fetchedData={fetchedData as GraphResponse}
                                kind={state.kindName!}
                            />
                        );
                    }
                })()}
            </div>
        </>
    );
}
