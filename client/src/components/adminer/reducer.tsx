import type { AdminerState, AdminerStateAction } from '@/types/adminer/Reducer';
import { type ColumnFilter, Operator } from '@/types/adminer/ColumnFilter';

export function reducer(state: AdminerState, action: AdminerStateAction): AdminerState {
    switch (action.type) {
    case 'datasource': {
        return {
            form: { limit: 50, filters: [] },
            active: { limit: 50, filters: [] },
            datasource: action.newDatasource,
            view: state.view,
        };
    }
    case 'kind': {
        return {
            ...state,
            form: { limit: 50, filters: [] },
            active: { limit: 50, filters: [] },
            kind: action.newKind,
        };
    }
    case 'view': {
        return {
            ...state,
            view: action.newView,
        };
    }
    case 'submit_state': {
        const activeLimit: number = state.form.limit;
        const activeFilters: ColumnFilter[] = state.form.filters.map(filter => ({
            ...filter,
        }));

        return {
            ...state,
            active: {
                limit: activeLimit,
                filters: activeFilters,
            },
        };
    }
    case 'change_limit': {
        return {
            ...state,
            form: {
                ...state.form,
                limit: action.newLimit,
            },
        };
    }
    case 'change_column_name': {
        const newFilters: ColumnFilter[] = [ ...state.form.filters ];
        newFilters[action.filterId].columnName = action.newName;

        return {
            ...state,
            form: {
                ...state.form,
                filters: newFilters,
            },
        };
    }
    case 'change_operator': {
        const newFilters: ColumnFilter[] = [ ...state.form.filters ];
        newFilters[action.filterId].operator = action.newOperator;

        return {
            ...state,
            form: {
                ...state.form,
                filters: newFilters,
            },
        };
    }
    case 'change_column_value': {
        const newFilters: ColumnFilter[] = [ ...state.form.filters ];
        newFilters[action.filterId].columnValue = action.newValue;

        return {
            ...state,
            form: {
                ...state.form,
                filters: newFilters,
            },
        };
    }
    case 'add_filter': {
        const nextId: number = state.form.filters ? state.form.filters.length : 0;
        const newFilter: ColumnFilter = { id: nextId, columnName: '', operator: Operator.eq, columnValue: '' };
        const newFilters: ColumnFilter[] = [ ...state.form.filters, newFilter ];

        return {
            ...state,
            form: {
                ...state.form,
                filters: newFilters,
            },
        };
    }
    case 'delete_filter': {
        const activeFilters: ColumnFilter[] = state.active.filters.filter(filter => filter.id !== action.filterID);
        const newFilters: ColumnFilter[] = state.form.filters.filter(filter => filter.id !== action.filterID);

        return {
            ...state,
            active: {
                ...state.active,
                filters: activeFilters,
            },
            form: {
                ...state.form,
                filters: newFilters,
            },
        };
    }
    case 'delete_filters': {
        return {
            ...state,
            form: { limit: 50, filters: [] },
            active: { limit: 50, filters: [] },
        };
    }
    default: {
        throw new Error('Unknown action');
    }
    }
}
