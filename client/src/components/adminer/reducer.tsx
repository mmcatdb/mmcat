import type { AdminerFilterState, AdminerFilterAction } from '@/types/adminer/Reducer';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';

export function reducer(state: AdminerFilterState, action: AdminerFilterAction): AdminerFilterState {
    const emptyState = { submitted: { limit: 50, filters: [] }, new: { limit: 50, filters: [] } };

    switch (action.type) {
    case 'submit_state': {
        return {
            ...state,
            submitted: {
                ...state.new,
            },
        };
    }
    case 'change_limit': {
        return {
            ...state,
            new: {
                ...state.new,
                limit: action.newLimit,
            },
        };
    }
    case 'change_column_name': {
        const newFilters: ColumnFilter[] = [ ...state.new.filters ];
        newFilters[action.filterId].columnName = action.newName;

        return {
            ...state,
            new: {
                ...state.new,
                filters: newFilters,
            },
        };
    }
    case 'change_operator': {
        const newFilters: ColumnFilter[] = [ ...state.new.filters ];
        newFilters[action.filterId].operator = action.newOperator;

        return {
            ...state,
            new: {
                ...state.new,
                filters: newFilters,
            },
        };
    }
    case 'change_column_value': {
        const newFilters: ColumnFilter[] = [ ...state.new.filters ];
        newFilters[action.filterId].columnValue = action.newValue;

        return {
            ...state,
            new: {
                ...state.new,
                filters: newFilters,
            },
        };
    }
    case 'add_filter': {
        const nextId: number = state.new.filters ? state.new.filters.length : 0;
        const newFilter: ColumnFilter = { id: nextId, columnName: '', operator: Operator.eq, columnValue: '' };
        const newFilters: ColumnFilter[] = [ ...state.new.filters, newFilter ];

        return {
            ...state,
            new: {
                ...state.new,
                filters: newFilters,
            },
        };
    }
    case 'delete_filter': {
        const submittedFilters: ColumnFilter[] = state.submitted.filters.filter(filter => filter.id !== action.filterID);
        const newFilters: ColumnFilter[] = state.new.filters.filter(filter => filter.id !== action.filterID);

        return {
            submitted: {
                ...state.submitted,
                filters: submittedFilters,
            },
            new: {
                ...state.new,
                filters: newFilters,
            },
        };
    }
    case 'delete_filters': {
        return emptyState;
    }
    default: {
        throw new Error('Unknown action');
    }
    }
}
