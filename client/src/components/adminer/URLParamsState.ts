import { AVAILABLE_VIEWS } from '@/components/adminer/Views';
import { View } from '@/types/adminer/View';
import { Operator } from '@/types/adminer/Operators';
import { QueryType } from '@/types/adminer/QueryType';
import type { Datasource } from '@/types/Datasource';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { PropertyFilter } from '@/types/adminer/PropertyFilter';
import { getInitPaginationState, type PaginationState, type ActiveAdminerState, type AdminerFilterQueryState, type KindFilterState, DEFAULT_LIMIT, DEFAULT_OFFSET } from '@/components/adminer/adminerReducer';

export function getInitURLParams(previousParams: URLSearchParams): URLSearchParams {
    const queryType = getQueryTypeFromURLParams(previousParams);
    const params = getParamsWithQueryType(queryType);
    return params;
}

export function getQueryTypeFromURLParams(params: URLSearchParams): QueryType | undefined {
    const queryTypeParam = params.get('queryType');
    const queryType = Object.values(QueryType).includes(queryTypeParam as QueryType)
        ? (queryTypeParam as QueryType)
        : undefined;
    return queryType;
}

export function getFiltersURLParam(filterState: KindFilterState): string {
    return JSON.stringify(filterState.propertyFilters);
}

export function getAdminerURLParams(previousParams: URLSearchParams, queryType: QueryType | undefined): URLSearchParams {
    const previousQueryType = getQueryTypeFromURLParams(previousParams);

    if (previousQueryType === undefined || previousQueryType !== queryType) {
        const params = new  URLSearchParams();
        params.set('queryType', queryType ?? QueryType.filter);

        const datasourceId = previousParams.get('datasourceId');
        if (datasourceId)
            params.set('datasourceId', datasourceId);

        return params;
    }

    previousParams.set('queryType', queryType ?? QueryType.filter);
    return previousParams;
}

export function getURLParamsFromCustomQueryState(query: string, datasource: Datasource): URLSearchParams {
    const params = getParamsWithQueryType(QueryType.custom);
    params.set('query', query);
    params.set('datasourceId', datasource.id);

    return params;
}

export function getURLParamsFromFilterQueryState(state: AdminerFilterQueryState | ActiveAdminerState): URLSearchParams {
    const params = getParamsWithQueryType(QueryType.filter);

    params.set('limit', String(state.active.limit));
    params.set('offset', String(state.active.offset));
    params.set('filters', getFiltersURLParam(state.active));

    if ('pagination' in state)
        params.set('pagination', getPaginationURLParam(state.pagination));

    if (state.datasourceId)
        params.set('datasourceId', state.datasourceId);
    if (state.kindName)
        params.set('kindName', state.kindName);
    params.set('view', state.view);

    return params;
}

function getParamsWithQueryType(queryType: QueryType | undefined): URLSearchParams {
    const params = new URLSearchParams();
    params.set('queryType', queryType ?? QueryType.filter);
    return params;
}

function getPaginationURLParam(pagination: PaginationState): string {
    return JSON.stringify(pagination);
}

export function getFilterQueryStateFromURLParams(params: URLSearchParams): AdminerFilterQueryState {
    const viewParam = params.get('view');
    const view = Object.values(View).includes(viewParam as View)
        ? (viewParam as View)
        : View.table;

    const kindFilters: KindFilterState = getKindFiltersFromURLParams(params);

    const paramsPagination = params.get('pagination');
    const pagination: PaginationState = paramsPagination != null
        ? JSON.parse(paramsPagination) as PaginationState
        : getInitPaginationState();

    return {
        form: kindFilters,
        active: kindFilters,
        datasourceId: params.get('datasourceId') ?? undefined,
        kindName: params.get('kindName') ?? undefined,
        view: view,
        pagination: pagination,
    };
}

export function getCustomQueryStateFromURLParams(params: URLSearchParams): { query?: string, datasourceId?: string } {
    return {
        query: params.get('query') ?? '',
        datasourceId: params.get('datasourceId') ?? undefined,
    };
}

function getKindFiltersFromURLParams(params: URLSearchParams): KindFilterState {
    const paramsLimit = params.get('limit');
    const paramsOffset = params.get('offset');
    const paramsFilters = params.get('filters');

    return {
        limit: paramsLimit ? parseInt(paramsLimit) : DEFAULT_LIMIT,
        offset: paramsOffset ? parseInt(paramsOffset) : DEFAULT_OFFSET,
        propertyFilters: JSON.parse(paramsFilters ?? '[]') as PropertyFilter[],
    };
}

export function getHrefFromReference(reference: KindReference, item: Record<string, unknown>, propertyName: string, datasources: Datasource[]): string {
    const state: ActiveAdminerState = {
        active: {
            limit: DEFAULT_LIMIT,
            offset: DEFAULT_OFFSET,
            propertyFilters: [
                {
                    id: 0,
                    propertyName: reference.property,
                    operator: Operator.Equal,
                    propertyValue: item[propertyName] as string,
                },
            ],
        },
        datasourceId: reference.datasourceId,
        kindName: reference.kindName,
        view: AVAILABLE_VIEWS[datasources.find(source => source.id === reference.datasourceId)!.type][0],
    };

    const urlParams = getURLParamsFromFilterQueryState(state);

    return urlParams.toString();
}
