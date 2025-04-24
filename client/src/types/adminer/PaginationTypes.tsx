import type { AdminerTypedAction } from '@/types/adminer/ReducerTypes';

export type PaginationState = {
    currentPage: number;
    offset: number;
    itemCount: number | undefined;
    totalPages: number;
};

export type PaginationAction =
| InitializeAction
| CurrentPageAction
| OffsetAction
| ItemCountAction
| TotalPagesAction;

type InitializeAction = AdminerTypedAction<'initialize'>;
type CurrentPageAction = AdminerTypedAction<'currentPage', { newCurrentPage: number }>;
type OffsetAction = AdminerTypedAction<'offset', { newOffset: number }>;
type ItemCountAction = AdminerTypedAction<'itemCount', { newItemCount: number | undefined }>;
type TotalPagesAction = AdminerTypedAction<'totalPages', { newTotalPages: number }>;
