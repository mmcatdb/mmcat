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
    case 'submit': {
        return {
            ...state,
            active: {
                limit: state.form.limit,
                filters: state.form.filters.map(filter => ({ ...filter })),
            },
        };
    }
    case 'input': {
        const { field, value } = action;

        if (field === 'limit') {
            return {
                ...state,
                form: {
                    ...state.form,
                    limit: value,
                },
            };
        }
        else {
            const updatedFilters = state.form.filters.map(filter =>
                filter.id === action.id
                    ? { ...filter, [field]: value }
                    : filter,
            );

            return {
                ...state,
                form: {
                    ...state.form,
                    filters: updatedFilters,
                },
            };
        }
    }
    case 'form': {
        const { action: formAction } = action;

        switch (formAction) {
        case 'add_filter': {
            const nextId = state.form.filters ? state.form.filters.length : 0;
            const newFilter: ColumnFilter = {
                id: nextId,
                columnName: '',
                operator: Operator.eq,
                columnValue: '',
            };
            return {
                ...state,
                form: { ...state.form, filters: [ ...state.form.filters, newFilter ] },
            };
        }
        case 'delete_filter': {
            const activeFilters = state.active.filters.filter(filter => filter.id !== action.id);
            const newFilters = state.form.filters.filter(filter => filter.id !== action.id);
            return {
                ...state,
                active: {
                    ...state.active,
                    filters: activeFilters,
                },
                form: { ...state.form, filters: newFilters },
            };
        }
        case 'delete_filters': {
            return {
                ...state,
                form: { limit: 50, filters: [] },
                active: { limit: 50, filters: [] },
            };
        }
        default:
            throw new Error('Unknown action');
        }
    }
    default:
        throw new Error('Unknown action');
    }
}

