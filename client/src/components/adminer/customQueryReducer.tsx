import type { AdminerCustomQueryState, AdminerCustomQueryStateAction } from '@/types/adminer/ReducerTypes';

export function customQueryReducer(state: AdminerCustomQueryState, action: AdminerCustomQueryStateAction): AdminerCustomQueryState {
    switch (action.type) {
    case 'initialize': {
        return {
            datasourceId: undefined,
            query: '',
        };
    }
    case 'datasource': {
        return {
            ...state,
            datasourceId: action.newDatasource.id,
        };
    }
    case 'query': {
        return {
            ...state,
            query: action.newQuery,
        };
    }
    case 'update': {
        return action.newState;
    }
    default:
        throw new Error('Unknown action');
    }
}
