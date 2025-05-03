import { View } from '@/types/adminer/View';
import { Operator } from '@/types/adminer/Operators';
import type { Datasource } from '@/types/datasource/Datasource';
import { getInitPaginationState, type PaginationState, type ActiveAdminerState, type AdminerFilterQueryState, type KindFilterState } from '@/components/adminer/filterQueryReducer';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import { AVAILABLE_VIEWS } from '@/components/adminer/Views';
import { QueryType } from '@/types/adminer/QueryType';

export function getQueryTypeFromURLParams(params: URLSearchParams): QueryType | undefined {
    const queryTypeParam = params.get('queryType');
    const queryType = Object.values(QueryType).includes(queryTypeParam as QueryType)
        ? (queryTypeParam as QueryType)
        : undefined;
    return queryType;
}

export function getFiltersURLParam(filterState: KindFilterState): string {
    return JSON.stringify(filterState.filters);
}

function getPaginationURLParam(pagination: PaginationState): string {
    return JSON.stringify(pagination);
}

function getParamsWithQueryType(queryType: QueryType | undefined): URLSearchParams {
    const params = new URLSearchParams();
    params.set('queryType', queryType ?? QueryType.filter);
    return params;
}

export function getInitURLParams(previousParams: URLSearchParams): URLSearchParams {
    const queryType = getQueryTypeFromURLParams(previousParams);
    const params = getParamsWithQueryType(queryType);
    return params;
}

export function getAdminerURLParams(previousParams: URLSearchParams, queryType: QueryType | undefined): URLSearchParams {
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

export function getHrefFromReference(reference: KindReference, item: Record<string, unknown>, propertyName: string, datasources: Datasource[]): string {
    const state: ActiveAdminerState = {
        active: {
            limit: 50,
            offset: 0,
            filters: [
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

export function getFilterQueryStateFromURLParams(params: URLSearchParams): AdminerFilterQueryState {
    const viewParam = params.get('view');
    const view = Object.values(View).includes(viewParam as View)
        ? (viewParam as View)
        : View.table;
    const filters: KindFilterState = JSON.parse(`{"limit":${params.get('limit') ?? 50},"offset":${params.get('offset') ?? 0},"filters":${params.get('filters') ?? '[]'}}`) as KindFilterState;
    const paramsPagination = params.get('pagination');
    const pagination: PaginationState = paramsPagination != null
        ? JSON.parse(paramsPagination) as PaginationState
        : getInitPaginationState();

    return {
        form: filters,
        active: filters,
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
