import { api } from '@/api';
import { View } from '@/types/adminer/View';
import { type Datasource } from '@/types/datasource';
import type { AdminerState, KindFilterState } from '@/types/adminer/Reducer';

export function getURLParamsFromState(state: AdminerState): URLSearchParams {
    const params = new URLSearchParams();

    params.set('active', JSON.stringify(state.active));
    if (state.datasource)
        params.set('datasource', state.datasource.id);
    if (state.kind)
        params.set('kind', state.kind);
    params.set('view', state.view);

    return params;
}

export async function getStateFromURLParams(params: URLSearchParams): Promise<AdminerState> {
    const datasourceId = params.get('datasource') ?? undefined;
    let datasource: Datasource | undefined = undefined;

    if (datasourceId !== undefined) {
        const response = await api.datasources.getDatasource({ id: datasourceId });

        if (response.status && response.data)
            datasource = response.data;
    }

    const viewParam = params.get('view');
    const view = Object.values(View).includes(viewParam as View)
        ? (viewParam as View)
        : View.table;

    return {
        form: JSON.parse(params.get('active') ?? '{"limit":10,"filters":[]}') as KindFilterState,
        active: JSON.parse(params.get('active') ?? '{"limit":10,"filters":[]}') as KindFilterState,
        datasource: datasource,
        kind: params.get('kind') ?? undefined,
        view: view,
    };
}
