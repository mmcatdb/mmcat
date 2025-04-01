import { getNewView } from './Views';
import { View } from '@/types/adminer/View';
import { Operator, UNARY_OPERATORS } from '@/types/adminer/Operators';
import type { PropertyFilter } from '@/types/adminer/PropertyFilter';
import type { AdminerState, AdminerStateAction, FormAction, InputAction } from '@/types/adminer/Reducer';

export function reducer(state: AdminerState, action: AdminerStateAction): AdminerState {
    switch (action.type) {
    case 'initialize': {
        return {
            form: { limit: 50, offset: 0, filters: [] },
            active: { limit: 50, offset: 0, filters: [] },
            view: View.table,
            datasourceId: undefined,
            kindName: undefined,
        };
    }
    case 'update': {
        return action.newState;
    }
    case 'datasource': {
        return {
            form: { limit: 50, offset: 0, filters: [] },
            active: { limit: 50, offset: 0, filters: [] },
            datasourceId: action.newDatasource.id,
            view: getNewView(state.view, action.newDatasource.type),
        };
    }
    case 'kind': {
        return {
            ...state,
            form: { limit: 50, offset: 0, filters: [] },
            active: { limit: 50, offset: 0, filters: [] },
            kindName: action.newKind,
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
                offset: state.form.offset,
                filters: state.form.filters.map(filter => ({ ...filter })),
            },
        };
    }
    case 'input': {
        return reducerInput(state, action);
    }
    case 'form': {
        return reducerForm(state, action);
    }
    default:
        throw new Error('Unknown action');
    }
}

function reducerInput(state: AdminerState, action: InputAction): AdminerState {
    const { field, value } = action;

    switch (field) {
    case 'limit':
    case 'offset':
        return { ...state, form: { ...state.form, [field]: action.value } };
    case 'propertyName':
    case 'propertyValue':
    case 'operator': {
        const updatedFilters = state.form.filters.map(filter => {
            if (filter.id === action.id) {
                const updatedFilter = { ...filter, [field]: value };

                if (field === 'operator' && UNARY_OPERATORS.includes(value))
                    updatedFilter.propertyValue = '';


                return updatedFilter;
            }
            return filter;
        });
        return { ...state, form: { ...state.form, filters: updatedFilters } };
    }
    }
}

function reducerForm(state: AdminerState, action: FormAction): AdminerState {
    const { action: formAction } = action;

    switch (formAction) {
    case 'add_filter': {
        const nextId = state.form.filters ? state.form.filters.length : 0;
        const newFilter: PropertyFilter = {
            id: nextId,
            propertyName: '',
            operator: Operator.Equal,
            propertyValue: '',
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
            form: { limit: 50, offset: 0, filters: [] },
            active: { limit: 50, offset: 0, filters: [] },
        };
    }
    default:
        throw new Error('Unknown action');
    }
}
