import { createContext, type ReactNode, useContext, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { FaSave, FaSpinner } from 'react-icons/fa';
import { api } from '@/api';
import { Category } from '@/types/schema';
import { onSuccess } from '@/types/api/result';
import { detectUnsavedChanges } from '@/pages/category/CategoryEditorPage';
import { type CategoryEditorState } from './useCategoryEditor';

type SaveState = {
    /** Whether there are unsaved changes */
    hasUnsavedChanges: boolean;
    /** Whether the save operation is in progress */
    isSaving: boolean;
    /** Function to handle saving changes */
    handleSave: (options?: { navigate?: (path: string) => void }) => Promise<void>;
};

const SaveContext = createContext<SaveState | undefined>(undefined);

type SaveProviderProps = {
    children: ReactNode;
    /** Category state to monitor for unsaved changes */
    categoryState?: CategoryEditorState;
};

/**
 * SaveProvider component to manage save state in schema editor and handle saving changes.
 */
export function SaveProvider({ children, categoryState }: SaveProviderProps) {
    const [ hasUnsavedChanges, setHasUnsavedChanges ] = useState(false);
    const [ isSaving, setIsSaving ] = useState(false);

    // Detect unsaved changes for category
    useEffect(() => {
        const checkForChanges = () => {
            let unsaved = false;

            // Category changes
            if (categoryState)
                unsaved = unsaved || detectUnsavedChanges(categoryState);

            setHasUnsavedChanges(unsaved);
        };

        checkForChanges();
        const interval = setInterval(checkForChanges, 1000);
        return () => clearInterval(interval);
    }, [ categoryState ]);

    // Unified save handler
    async function handleSave() {
        if (isSaving)
            return;

        setIsSaving(true);
        try {
            // Save category
            if (categoryState && hasUnsavedChanges) {
                await categoryState.evocat.update(async edit => {
                    const response = await api.schemas.updateCategory(
                        { id: categoryState.evocat.category.id },
                        edit,
                    );
                    if (!response.status) {
                        throw new Error(
                            typeof response.error === 'string' ? response.error : 'Failed to save category',
                        );
                    }
                    return onSuccess(response, response => Category.fromResponse(response));
                });
            }

            setHasUnsavedChanges(false);
            toast.success('Changes saved successfully');
        }
        catch (err) {
            toast.error('Failed to save changes', { autoClose: 5000 });
            console.error('Save Error:', err);
        }
        finally {
            setIsSaving(false);
        }
    }

    return (
        <SaveContext.Provider value={{ hasUnsavedChanges, isSaving, handleSave }}>
            {children}
        </SaveContext.Provider>
    );
}

export function useSave() {
    const context = useContext(SaveContext);
    if (!context)
        throw new Error('useSave must be used within a SaveProvider');

    return context;
}

// Reusable SaveButton component
export function SaveButton() {
    const { hasUnsavedChanges, isSaving, handleSave } = useSave();

    return (
        <div
            id='save-button'
            className='flex items-center gap-1 text-default-600 hover:text-default-800 cursor-pointer relative'
            onClick={() => handleSave()}
            title='Save Changes (Ctrl+S)'
        >
            {isSaving ? (
                <FaSpinner className='animate-spin' size={18} />
            ) : (
                <FaSave size={18} />
            )}
            {hasUnsavedChanges && !isSaving && (
                <span className='text-warning-600 text-sm font-bold absolute -top-2 right-0'>*</span>
            )}
        </div>
    );
}
