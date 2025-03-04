import { View } from '@/types/adminer/View';
import { Operator } from '@/types/adminer/Operators';
import type { Datasource } from '@/types/datasource/Datasource';
import type { ActiveAdminerState, AdminerState, KindFilterState } from '@/types/adminer/Reducer';
import type { AdminerReference } from '@/types/adminer/AdminerReferences';
import { AVAILABLE_VIEWS } from '@/components/adminer/Views';

export function getURLParamsFromState(state: AdminerState | ActiveAdminerState): URLSearchParams {
    const params = new URLSearchParams();

    params.set('active', JSON.stringify(state.active));
    if (state.datasourceId)
        params.set('datasourceId', state.datasourceId);
    if (state.kindName)
        params.set('kindName', state.kindName);
    params.set('view', state.view);

    return params;
}

export function getHrefFromReference(reference: AdminerReference, item: Record<string, unknown>, column: string, datasources: Datasource[]): string {
    const state: ActiveAdminerState = {
        active: {
            limit: 50,
            filters: [
                {
                    id: 0,
                    propertyName: reference.referencingProperty,
                    operator: Operator.Equal,
                    propertyValue: item[column] as string,
                },
            ],
        },
        datasourceId: reference.referencedDatasourceId,
        kindName: reference.referencingKindName,
        view: AVAILABLE_VIEWS[datasources.find(source => source.id === reference.referencedDatasourceId)!.type][0],
    };

    const urlParams = getURLParamsFromState(state);

    return urlParams.toString();
}

export function getStateFromURLParams(params: URLSearchParams): AdminerState {
    const viewParam = params.get('view');
    const view = Object.values(View).includes(viewParam as View)
        ? (viewParam as View)
        : View.table;
    const filters: KindFilterState = JSON.parse(params.get('active') ?? '{"limit":50,"filters":[]}') as KindFilterState;

    return {
        form: filters,
        active: filters,
        datasourceId: params.get('datasourceId') ?? undefined,
        kindName: params.get('kindName') ?? undefined,
        view: view,
    };
}
