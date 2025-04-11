import type { AdminerCustomQueryState, AdminerCustomQueryStateAction } from '@/types/adminer/ReducerTypes';

export function customQueryReducer(state: AdminerCustomQueryState, action: AdminerCustomQueryStateAction): AdminerCustomQueryState {
    switch (action.type) {
    case 'initialize': {
        return {
            datasourceId: undefined,
            query: '',
            unsubmittedQuery: '',
        };
    }
    case 'datasource': {
        return {
            datasourceId: action.newDatasource.id,
            query: '',
            unsubmittedQuery: '',
        };
    }
    case 'query': {
        return {
            ...state,
            unsubmittedQuery: action.newQuery,
        };
    }
    case 'update': {
        return action.newState;
    }
    case 'submit': {
        return {
            ...state,
            query: state.unsubmittedQuery,
        };
    }
    default:
        throw new Error('Unknown action');
    }
}
