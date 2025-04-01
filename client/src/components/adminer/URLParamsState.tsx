import { View } from '@/types/adminer/View';
import { Operator } from '@/types/adminer/Operators';
import type { Datasource } from '@/types/datasource/Datasource';
import type { ActiveAdminerState, AdminerState, KindFilterState } from '@/types/adminer/Reducer';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import { AVAILABLE_VIEWS } from '@/components/adminer/Views';

export function getFiltersURLParam(filterState: KindFilterState): string {
    return JSON.stringify(filterState.filters);
}

export function getURLParamsFromState(state: AdminerState | ActiveAdminerState): URLSearchParams {
    const params = new URLSearchParams();

    params.set('limit', String(state.active.limit));
    params.set('offset', String(state.active.offset));
    params.set('filters', getFiltersURLParam(state.active));

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

    const urlParams = getURLParamsFromState(state);

    return urlParams.toString();
}

export function getStateFromURLParams(params: URLSearchParams): AdminerState {
    const viewParam = params.get('view');
    const view = Object.values(View).includes(viewParam as View)
        ? (viewParam as View)
        : View.table;
    const filters: KindFilterState = JSON.parse(`{"limit":${params.get('limit') ?? 50},"offset":${params.get('offset') ?? 0},"filters":${params.get('filters') ?? '[]'}}`) as KindFilterState;

    return {
        form: filters,
        active: filters,
        datasourceId: params.get('datasourceId') ?? undefined,
        kindName: params.get('kindName') ?? undefined,
        view: view,
    };
}
