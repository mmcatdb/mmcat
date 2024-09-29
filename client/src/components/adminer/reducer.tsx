import type { State, Action } from '@/types/adminer/Reducer';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';

export function reducer(state: State, action: Action): State {
    switch (action.type) {
    case 'change_limit': {
        return {
            ...state,
            limit: action.newLimit,
        };
    }
    case 'change_column_name': {
        const newFilters: ColumnFilter[] = [ ...state.filters ];
        newFilters[action.filterId].columnName = action.newName;

        return {
            ...state,
            filters: newFilters,
        };
    }
    case 'change_operator': {
        const newFilters: ColumnFilter[] = [ ...state.filters ];
        newFilters[action.filterId].operator = action.newOperator;

        return {
            ...state,
            filters: newFilters,
        };
    }
    case 'change_column_value': {
        const newFilters: ColumnFilter[] = [ ...state.filters ];
        newFilters[action.filterId].columnValue = action.newValue;

        return {
            ...state,
            filters: newFilters,
        };
    }
    case 'add_filter': {
        const nextId: number = state.filters ? state.filters.length : 0;
        const newFilter: ColumnFilter = { id: nextId, columnName: '', operator: Operator.eq, columnValue: '' };
        const newFilters: ColumnFilter[] = [ ...state.filters, newFilter ];

        return {
            ...state,
            filters: newFilters,
        };
    }
    case 'delete_filters': {
        return {
            ...state,
            filters: [],
        };
    }
    default: {
        throw new Error('Unknown action');
    }
    }
}
