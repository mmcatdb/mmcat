import clsx from 'clsx';
import { useCallback, useEffect, useMemo, useReducer, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Spinner, Pagination } from '@nextui-org/react';
import { api } from '@/api';
import { usePreferences } from '@/components/PreferencesProvider';
import { getFilterQueryStateFromURLParams, getFiltersURLParam, getURLParamsFromFilterQueryState } from '@/components/adminer/URLParamsState';
import { FilterForm } from '@/components/adminer/FilterForm';
import { KindMenu, UNLABELED } from '@/components/adminer/KindMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { ExportComponent } from '@/components/adminer/ExportComponent';
import { filterQueryReducer } from '@/components/adminer/adminerReducer';
import { useFetchReferences } from '@/components/adminer/useFetchReferences';
import { useFetchData } from '@/components/adminer/useFetchData';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { View } from '@/types/adminer/View';
import { type Datasource, DatasourceType } from '@/types/datasource';
import type { Id } from '@/types/id';
import type { QueryParams } from '@/types/api/routes';
import type { DataResponse } from '@/types/adminer/DataResponse';
import type { KindFilterState } from '@/components/adminer/adminerReducer';
import type { AdminerReferences, KindReference } from '@/types/adminer/AdminerReferences';

type AdminerFilterQueryPageProps = Readonly<{
    datasource: Datasource;
    datasources: Datasource[];
}>;

export function AdminerFilterQueryPage({ datasource, datasources }: AdminerFilterQueryPageProps) {
    const { theme } = usePreferences().preferences;
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ state, dispatch ] = useReducer(filterQueryReducer, searchParams, getFilterQueryStateFromURLParams);
    const [ kindReferences, setKindReferences ] = useState<KindReference[]>([]);
    const stateRef = useRef(state);
    const searchParamsRef = useRef(searchParams);

    const { references, referencesLoading } = useFetchReferences(state);

    useEffect(() => {
        dispatch({ type:'datasource', newDatasource: datasource });
    }, [ datasource ]);

    // Sync state with URL search parameters
    useEffect(() => {
        if (JSON.stringify(searchParamsRef.current) !== JSON.stringify(searchParams)) {
            dispatch({ type:'update', newState: getFilterQueryStateFromURLParams(searchParams) });
            searchParamsRef.current = searchParams;
        }
    }, [ searchParams ]);

    // Update URL search parameters whenever state changes
    useEffect(() => {
        if (stateRef.current !== state) {
            setSearchParams(getURLParamsFromFilterQueryState(state));
            stateRef.current = state;
        }
    }, [ state ]);

    const fetchFunction = useCallback(() => {
        if (!state.datasourceId || !state.kindName) {
            return Promise.resolve({
                status: false as const,
                error: undefined,
            });
        }

        return api.adminer.getKind({ datasourceId: state.datasourceId }, getQueryParams(state.kindName, state.active));
    }, [ state.datasourceId, state.kindName, state.active ]);

    const { fetchedData, loading, error } = useFetchData<DataResponse>(fetchFunction);

    useEffect(() => {
        dispatch({ type: 'itemCount', newItemCount: fetchedData?.metadata.itemCount });
    }, [ fetchedData?.metadata.itemCount ]);

    useMemo(() => {
        if (state.datasourceId && state.kindName)
            setKindReferences(computeKindReferences(references, state.datasourceId, state.kindName));
    }, [ references, state.datasourceId, state.kindName ]);

    return (
        <>
            <div className={clsx(
                'grid grid-flow-col grid-rows-2 border-b px-0 py-1 gap-2',
                theme === 'dark' ? 'border-gray-700' : 'border-gray-300',
            )}>
                <div className='flex items-start'>
                    <KindMenu datasourceId={datasource.id} kind={state.kindName} showUnlabeled={datasource.type === DatasourceType.neo4j} dispatch={dispatch}/>

                    {state.kindName !== undefined && (
                        <ViewMenu datasourceType={datasource.type} view={state.view} dispatch={dispatch}/>
                    )}
                </div>

                {datasource && state.kindName && state.pagination.itemCount !== undefined && state.pagination.itemCount > 0 && (
                    <div className='inline-flex gap-2 items-center self-end'>
                        {state.view !== View.graph && (
                            <>
                                <Pagination
                                    size='sm'
                                    total={state.pagination.totalPages}
                                    page={state.pagination.currentPage}
                                    onChange={page => dispatch({ type: 'page', newCurrentPage: page, newOffset: state.active.limit * (page - 1) })}
                                    color='primary'
                                />
                                <p className='min-w-36'>Number of rows: {state.pagination.itemCount}</p>
                            </>
                        )}

                        {fetchedData && (
                            <ExportComponent data={fetchedData}/>
                        )}
                    </div>
                )}

                <div className='row-span-2 justify-self-end'>
                    {datasource && state.kindName && (
                        <FilterForm state={state} datasourceType={datasources.find(source => source.id === state.datasourceId)!.type} propertyNames={fetchedData?.metadata.propertyNames} dispatch={dispatch}/>
                    )}
                </div>
            </div>

            {state.kindName && error ? (
                <p>{error}</p>
            ) : (
                <>
                    {loading || referencesLoading && (
                        <div className='h-10 flex items-center justify-center'>
                            <Spinner />
                        </div>
                    )}

                    {state.kindName && state.datasourceId && fetchedData?.data && (
                        <div className='flex grow min-h-0 mt-2'>
                            <DatabaseView
                                view={state.view}
                                data={fetchedData}
                                kindReferences={kindReferences}
                                kindName={state.kindName}
                                datasourceId={state.datasourceId}
                                datasources={datasources}
                            />
                        </div>
                    )}
                </>
            )}
        </>
    );
}

function getQueryParams(kindName: string, filterState: KindFilterState): QueryParams {
    const queryParams: QueryParams = { limit: filterState.limit, offset: filterState.offset };

    if (filterState.propertyFilters.length > 0)
        queryParams.filters = getFiltersURLParam(filterState);

    if (kindName !== UNLABELED)
        queryParams.kindName = kindName;

    return queryParams;
}

function computeKindReferences(references: AdminerReferences, datasourceId: Id, kind: string): KindReference[] {
    return references
        ? Object.values(references)
            .filter(ref => ref.from.datasourceId === datasourceId && ref.from.kindName === kind)
            .map(ref => ({
                fromProperty: ref.from.property,
                datasourceId: ref.to.datasourceId,
                kindName: ref.to.kindName,
                property: ref.to.property,
            }))
        : [];
}
