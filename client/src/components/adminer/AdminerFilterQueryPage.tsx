import clsx from 'clsx';
import { useCallback, useEffect, useReducer, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Spinner, Pagination } from '@nextui-org/react';
import { api } from '@/api';
import { getFilterQueryStateFromURLParams, getFiltersURLParam, getURLParamsFromFilterQueryState } from '@/components/adminer/URLParamsState';
import { FilterForm } from '@/components/adminer/FilterForm';
import { KindMenu } from '@/components/adminer/KindMenu';
import { ViewMenu } from '@/components/adminer/ViewMenu';
import { ExportComponent } from '@/components/adminer/ExportComponent';
import { filterQueryReducer } from '@/components/adminer/filterQueryReducer';
import { getInitPaginationState, paginationReducer } from '@/components/adminer/paginationReducer';
import { useFetchReferences } from '@/components/adminer/useFetchReferences';
import { useFetchData } from '@/components/adminer/useFetchData';
import { DatabaseView } from '@/components/adminer/DatabaseView';
import { View } from '@/types/adminer/View';
import { type Datasource, DatasourceType } from '@/types/datasource';
import type { Id } from '@/types/id';
import type { QueryParams } from '@/types/api/routes';
import type { DataResponse } from '@/types/adminer/DataResponse';
import type { KindFilterState } from '@/types/adminer/ReducerTypes';
import type { AdminerReferences, KindReference } from '@/types/adminer/AdminerReferences';
import type { Theme } from '@/components/PreferencesProvider';

function getQueryParams(filterState: KindFilterState): QueryParams {
    if (filterState.filters.length > 0)
        return { filters: getFiltersURLParam(filterState), limit: filterState.limit, offset: filterState.offset };

    return { limit: filterState.limit, offset: filterState.offset };
}

function getKindReferences(references: AdminerReferences, datasourceId: Id, kind: string): KindReference[] {
    return references
        ? Object.values(references)
            .filter(ref => ref.referencingDatasourceId === datasourceId && ref.referencingKindName === kind)
            .map(ref => ({
                referencingProperty: ref.referencingProperty,
                datasourceId: ref.referencedDatasourceId,
                kindName: ref.referencedKindName,
                property: ref.referencedProperty,
            }))
        : [];
}

type AdminerFilterQueryPageProps = Readonly<{
    datasource: Datasource;
    datasources: Datasource[];
    theme: Theme;
}>;

export function AdminerFilterQueryPage({ datasource, datasources, theme }: AdminerFilterQueryPageProps) {
    const [ searchParams, setSearchParams ] = useSearchParams();
    const [ paginationState, paginationDispatch ] = useReducer(paginationReducer, getInitPaginationState());
    const [ state, dispatch ] = useReducer(filterQueryReducer, searchParams, getFilterQueryStateFromURLParams);
    const [ kindReferences, setKindReferences ] = useState<KindReference[]>([]);
    const stateRef = useRef(state);
    const searchParamsRef = useRef(searchParams);

    const { references, referencesLoading } = useFetchReferences(state);

    useEffect(() => {
        // FIXME Tuto kontrolu můžete provést v reduceru, kde máte k dispozici celý stav. Teď tady dostávám eslint warning na dependency array.
        if (state.datasourceId !== datasource.id)
            dispatch({ type:'datasource', newDatasource: datasource });
    }, [ datasource ]);

    // Sync state with URL search parameters
    useEffect(() => {
        if (searchParamsRef.current != searchParams) {
            dispatch({ type:'update', newState: getFilterQueryStateFromURLParams(searchParams) });
            searchParamsRef.current = searchParams;
        }
    }, [ searchParams ]);

    // Update URL search parameters whenever state changes
    // FIXME Toto se provádí i když se změní searchParams, ne jen state. Nicméně potom nikdy nebude platit searchParamsRef.current == searchParams, čili se to stejně neprovede. Takže bych to vyřadil z dependency array. Nešlo by vyřadit i tu podmínku 'searchParamsRef.current == searchParams'?
    useEffect(() => {
        if (stateRef.current != state && searchParamsRef.current == searchParams) {
            setSearchParams(getURLParamsFromFilterQueryState(state));
            stateRef.current = state;
        }
    }, [ state, searchParams ]);

    // FIXME Toto mi nepřijde úplně ideální. Hook useEffect je poslední možnost, pokud ostatní nefungují, ne default.
    // Dále, máte tu spoustu logiky z pagination. To má být naopak. V reduceru řešíte logiku, a tady máte jen "hloupé" volání dispatchu.
    // Nedává moc smysl mít useEffect, který reaguje na změnu stavu a rovnou volá dispatch. To se potom mělo vyřešit už v té počáteční změně stavu. Nebo dokonce dva dispatche hned za sebou, jak máte v Pagination.
    // Také je trochu neideální, jak mezi sebou dva reducery interagují. Spíš bych doporučoval přesunout pagination jako jednu property do stavu hlavního reduceru.

    useEffect(() => {
        dispatch({ type: 'input', field: 'offset', value: paginationState.offset });
    }, [ paginationState.offset ]);

    useEffect(() => {
        if (paginationState.itemCount)
            paginationDispatch({ type: 'totalPages', newTotalPages: Math.ceil(paginationState.itemCount / state.active.limit) });

        if (paginationState.currentPage > paginationState.totalPages) {
            paginationDispatch({ type: 'currentPage', newCurrentPage: paginationState.totalPages });
            paginationDispatch({ type: 'offset', newOffset: state.active.limit * (paginationState.totalPages - 1) });
        }
    }, [ paginationState.totalPages, paginationState.currentPage, paginationState.itemCount, state.active.limit ]);

    const fetchFunction = useCallback(() => {
        if (!state.datasourceId || !state.kindName) {
            return Promise.resolve({
                status: false as const,
                error: undefined,
            });
        }

        return api.adminer.getKind({ datasourceId: state.datasourceId, kindName: state.kindName }, getQueryParams(state.active));
    }, [ state.datasourceId, state.kindName, state.active ]);

    const { fetchedData, loading, error } = useFetchData<DataResponse>(fetchFunction);

    useEffect(() => {
        paginationDispatch({ type: 'initialize' });
    }, [ state.datasourceId, state.kindName ]);

    useEffect(() => {
        const count = fetchedData?.metadata.itemCount;
        paginationDispatch({ type: 'itemCount', newItemCount: count ?? 0 });
    }, [ fetchedData ]);

    // FIXME useMemo.
    // U funkcí, které něco počítají, bych spíš zvolil název "computeKindReferences".
    useEffect(() => {
        if (state.datasourceId && state.kindName)
            setKindReferences(getKindReferences(references, state.datasourceId, state.kindName));
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

                {datasource && state.kindName && typeof state.kindName === 'string' && paginationState.itemCount !== undefined && paginationState.itemCount > 0 && (
                    // FIXME Co jiného než string by to mohlo být?
                    <div className='inline-flex gap-2 items-center self-end'>
                        {state.view !== View.graph && (
                            <>
                                <Pagination
                                    size='sm'
                                    total={paginationState.totalPages}
                                    page={paginationState.currentPage}
                                    onChange={page => {
                                        paginationDispatch({ type: 'currentPage', newCurrentPage: page });
                                        paginationDispatch({ type: 'offset', newOffset: state.active.limit * (page - 1) });
                                    }}
                                    color='primary'
                                />
                                <p className='min-w-36'>Number of rows: {paginationState.itemCount}</p>
                            </>
                        )}

                        {fetchedData && (
                            <ExportComponent data={fetchedData}/>
                        )}
                    </div>
                )}

                <div className='row-span-2 justify-self-end'>
                    {datasource && state.kindName && typeof state.kindName === 'string' && (
                        <FilterForm state={state} datasourceType={datasources.find(source => source.id === state.datasourceId)!.type} propertyNames={fetchedData?.metadata.propertyNames} dispatch={dispatch}/>
                    )}
                </div>
            </div>

            {state.kindName && error ? (
                // FIXME Error a loading bych řešil výše a rovnou vrátil chybovou hlášku / spinner.
                // Podle props je datasource vždy definovaný, takže bych ho tady nekontroloval.
                <p>{error}</p>
            ) : (
                <>
                    {loading || referencesLoading && (
                        <div className='h-10 flex items-center justify-center'>
                            <Spinner />
                        </div>
                    )}

                    {datasource && state.kindName && typeof state.kindName === 'string' && fetchedData?.data && (
                        <div className='flex grow min-h-0 mt-2'>
                            <DatabaseView
                                view={state.view}
                                fetchedData={fetchedData}
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
