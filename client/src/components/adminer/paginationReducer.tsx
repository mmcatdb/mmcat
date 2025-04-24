import type { PaginationAction, PaginationState } from '@/types/adminer/PaginationTypes';

export function paginationReducer(state: PaginationState, action: PaginationAction): PaginationState {
    switch (action.type) {
    case 'initialize': {
        return getInitPaginationState();
    }
    case 'currentPage': {
        return {
            ...state,
            currentPage: action.newCurrentPage,
        };
    }
    case 'offset': {
        return {
            ...state,
            offset: action.newOffset,
        };
    }
    case 'itemCount': {
        return {
            ...state,
            itemCount: action.newItemCount,
        };
    }
    case 'totalPages': {
        return {
            ...state,
            totalPages: action.newTotalPages,
        };
    }
    default:
        throw new Error('Unknown action');
    }
}

export function getInitPaginationState(): PaginationState {
    return {
        currentPage: 1,
        offset: 0,
        itemCount: undefined,
        totalPages: 1,
    };
}
