import { getNewView } from './Views';
import { View } from '@/types/adminer/View';
import { Operator, UNARY_OPERATORS } from '@/types/adminer/Operators';
import type { PropertyFilter } from '@/types/adminer/PropertyFilter';
import type { Datasource } from '@/types/datasource';
import type { Id } from '@/types/id';

export const DEFAULT_LIMIT = 50;
export const DEFAULT_OFFSET = 0;

export function filterQueryReducer(state: AdminerFilterQueryState, action: AdminerFilterQueryStateAction): AdminerFilterQueryState {
    switch (action.type) {
    case 'initialize': {
        return {
            form: getInitKindFilterState(),
            active: getInitKindFilterState(),
            view: View.table,
            datasourceId: undefined,
            kindName: undefined,
            pagination: getInitPaginationState(),
        };
    }
    case 'update': {
        return action.newState;
    }
    case 'datasource': {
        if (state.datasourceId !== action.newDatasource.id) {
            return {
                form: getInitKindFilterState(),
                active: getInitKindFilterState(),
                datasourceId: action.newDatasource.id,
                view: getNewView(state.view, action.newDatasource.type),
                pagination: getInitPaginationState(),
            };
        }

        return state;
    }
    case 'kind': {
        return {
            ...state,
            form: getInitKindFilterState(),
            active: getInitKindFilterState(),
            kindName: action.newKind,
            pagination: getInitPaginationState(),
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
                propertyFilters: state.form.propertyFilters.map(filter => ({ ...filter })),
            },
        };
    }
    case 'input': {
        return reducerInput(state, action);
    }
    case 'form': {
        return reducerForm(state, action);
    }
    case 'itemCount': {
        return {
            ...state,
            pagination: {
                ...state.pagination,
                totalPages: action.newItemCount ? Math.ceil(action.newItemCount / state.active.limit) : 1,
                itemCount: action.newItemCount,
            },
        };
    }
    case 'page': {
        if (action.newCurrentPage > state.pagination.totalPages) {
            const newOffset = state.active.limit * (state.pagination.totalPages - 1);

            return {
                ...state,
                active: {
                    ...state.form,
                    offset: newOffset,
                },
                pagination: {
                    ...state.pagination,
                    currentPage: state.pagination.totalPages,
                    offset: newOffset,
                },
            };
        }

        return {
            ...state,
            active: {
                ...state.form,
                offset: action.newOffset,
            },
            pagination: {
                ...state.pagination,
                currentPage: action.newCurrentPage,
                offset: action.newOffset,
            },
        };
    }
    default:
        throw new Error('Unknown action');
    }
}

function reducerInput(state: AdminerFilterQueryState, action: InputAction): AdminerFilterQueryState {
    const { field, value } = action;

    switch (field) {
    case 'limit':
        return { ...state, form: { ...state.form, [field]: action.value } };
    case 'propertyName':
    case 'propertyValue':
    case 'operator': {
        const updatedFilters = state.form.propertyFilters.map(filter => {
            if (filter.id === action.id) {
                const updatedFilter = { ...filter, [field]: value };

                if (field === 'operator' && UNARY_OPERATORS.includes(value))
                    updatedFilter.propertyValue = '';


                return updatedFilter;
            }
            return filter;
        });
        return { ...state, form: { ...state.form, propertyFilters: updatedFilters } };
    }
    }
}

function reducerForm(state: AdminerFilterQueryState, action: FormAction): AdminerFilterQueryState {
    const { action: formAction } = action;

    switch (formAction) {
    case 'add_filter': {
        const nextId = state.form.propertyFilters ? state.form.propertyFilters.length : 0;
        const newFilter: PropertyFilter = {
            id: nextId,
            propertyName: '',
            operator: Operator.Equal,
            propertyValue: '',
        };
        return {
            ...state,
            form: { ...state.form, propertyFilters: [ ...state.form.propertyFilters, newFilter ] },
        };
    }
    case 'delete_filter': {
        const activeFilters = state.active.propertyFilters.filter(filter => filter.id !== action.id);
        const newFilters = state.form.propertyFilters.filter(filter => filter.id !== action.id);
        return {
            ...state,
            active: {
                ...state.active,
                propertyFilters: activeFilters,
            },
            form: { ...state.form, propertyFilters: newFilters },
        };
    }
    case 'delete_filters': {
        return {
            ...state,
            form: { limit: 50, offset: 0, propertyFilters: [] },
            active: { limit: 50, offset: 0, propertyFilters: [] },
        };
    }
    default:
        throw new Error('Unknown action');
    }
}

function getInitKindFilterState(): KindFilterState {
    return { limit: DEFAULT_LIMIT, offset: DEFAULT_OFFSET, propertyFilters: [] };
}

export function getInitPaginationState(): PaginationState {
    return {
        currentPage: 1,
        offset: 0,
        itemCount: undefined,
        totalPages: 1,
    };
}

export type AdminerFilterQueryState = ActiveAdminerState & {
    form: KindFilterState;
    pagination: PaginationState;
};

export type ActiveAdminerState = {
    active: KindFilterState;
    kindName?: string;
    view: View;
} & AdminerStateBase;

export type KindFilterState = {
    limit: number;
    offset: number;
    propertyFilters: PropertyFilter[];
};

type AdminerStateBase = {
    datasourceId?: Id;
};

export type PaginationState = {
    currentPage: number;
    offset: number;
    itemCount: number | undefined;
    totalPages: number;
};

export type AdminerFilterQueryStateAction =
| DatasourceAction
| KindAction
| ViewAction
| InputAction
| FormAction
| SubmitAction
| InitializeAction
| FilterQueryUpdateAction
| ItemCountAction
| NewPageAction;

type AdminerTypedAction<T extends string, P = undefined> = P extends undefined
  ? { type: T }
  : { type: T } & P;

type InputAction = AdminerTypedAction<'input', {
    field: 'limit';
    value: number;
} | {
    field: 'propertyName' | 'propertyValue';
    id: number;
    value: string;
} | {
    field: 'operator';
    id: number;
    value: Operator;
}>;

type FormAction = AdminerTypedAction<'form', {
    action: 'add_filter';
} | {
    action: 'delete_filter';
    id: number;
} | {
    action: 'delete_filters';
}>;

type DatasourceAction = AdminerTypedAction<'datasource', { newDatasource: Datasource }>;
type KindAction = AdminerTypedAction<'kind', { newKind: string }>;
type ViewAction = AdminerTypedAction<'view', { newView: View }>;
type SubmitAction = AdminerTypedAction<'submit'>;
type InitializeAction = AdminerTypedAction<'initialize'>;
type FilterQueryUpdateAction = AdminerTypedAction<'update', {newState: AdminerFilterQueryState }>;
type ItemCountAction = AdminerTypedAction<'itemCount', { newItemCount: number | undefined }>;
type NewPageAction = AdminerTypedAction<'page', { newCurrentPage: number, newOffset: number }>;
