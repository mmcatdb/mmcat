import { type CategoryEditorDispatch, type CategoryEditorState } from './useCategoryEditor';
import { SelectionDisplay } from './SelectionDisplay';
import { cn } from '@/components/common/utils';
import { ObjexEditor } from './ObjexEditor';
import { MorphismEditor } from './MorphismEditor';

type StateDispatchProps = {
    state: CategoryEditorState;
    dispatch: CategoryEditorDispatch;
};

type RightPanelEditorProps = StateDispatchProps & {
    className?: string;
};

export function RightPanelCategoryEditor({ state, dispatch, className }: RightPanelEditorProps) {
    const Component = getRightPanelComponent(state);

    return (
        <div className={cn('px-3 py-2 flex flex-col gap-2', className)}>
            <Component state={state} dispatch={dispatch} />
        </div>
    );
}

function getRightPanelComponent(state: CategoryEditorState) {
    if (state.form?.type === 'objex')
        return ObjexEditor;

    if (state.form?.type === 'morphism')
        return MorphismEditor;

    return SelectionDisplay;
}
