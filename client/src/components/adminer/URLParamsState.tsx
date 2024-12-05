import { View } from '@/types/adminer/View';
import type { AdminerState, KindFilterState } from '@/types/adminer/Reducer';

export function getURLParamsFromState(state: AdminerState): URLSearchParams {
    const params = new URLSearchParams();

    params.set('active', JSON.stringify(state.active));
    if (state.datasourceId)
        params.set('datasourceId', state.datasourceId);
    if (state.kindName)
        params.set('kindName', state.kindName);
    params.set('view', state.view);

    return params;
}

export function getStateFromURLParams(params: URLSearchParams): AdminerState {
    const viewParam = params.get('view');
    const view = Object.values(View).includes(viewParam as View)
        ? (viewParam as View)
        : View.table;

    return {
        form: JSON.parse(params.get('active') ?? '{"limit":50,"filters":[]}') as KindFilterState,
        active: JSON.parse(params.get('active') ?? '{"limit":50,"filters":[]}') as KindFilterState,
        datasourceId: params.get('datasourceId') ?? undefined,
        kindName: params.get('kindName') ?? undefined,
        view: view,
    };
}
