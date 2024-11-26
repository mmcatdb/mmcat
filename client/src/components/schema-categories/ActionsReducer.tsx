import { useReducer, useEffect } from 'react';
import { api } from '@/api';
import { toast } from 'react-toastify';
import { Action } from '@/types/action';
import { useCategoryInfo } from '../CategoryInfoProvider';

type State = {
    actions: Action[];
    loading: boolean;
    error: string | null;
    isModalOpen: boolean;
};

type ActionReducer =
    | { type: 'FETCH_START' }
    | { type: 'FETCH_SUCCESS', payload: Action[] }
    | { type: 'FETCH_ERROR', payload: string }
    | { type: 'ADD_ACTION', payload: Action }
    | { type: 'DELETE_ACTION', payload: string }
    | { type: 'TOGGLE_MODAL', payload: boolean };

const initialState: State = {
    actions: [],
    loading: false,
    error: null,
    isModalOpen: false,
};

function actionsReducer(state: State, action: ActionReducer): State {
    switch (action.type) {
    case 'FETCH_START':
        return { ...state, loading: true, error: null };
    case 'FETCH_SUCCESS':
        return { ...state, loading: false, actions: action.payload };
    case 'FETCH_ERROR':
        return { ...state, loading: false, error: action.payload };
    case 'ADD_ACTION':
        return { ...state, actions: [ ...state.actions, action.payload ] };
    case 'DELETE_ACTION':
        return {
            ...state,
            actions: state.actions.filter((a) => a.id !== action.payload),
        };
    case 'TOGGLE_MODAL':
        return { ...state, isModalOpen: action.payload };
    default:
        return state;
    }
}

export function useActions() {
    const [ state, dispatch ] = useReducer(actionsReducer, initialState);
    const { category } = useCategoryInfo();

    useEffect(() => {
        const fetchActions = async () => {
            dispatch({ type: 'FETCH_START' });
            try {
                const response = await api.actions.getAllActionsInCategory({
                    categoryId: category.id,
                });
                if (response.status) {
                    const actions = response.data.map(Action.fromServer);
                    dispatch({ type: 'FETCH_SUCCESS', payload: actions });
                }
                else {
                    dispatch({ type: 'FETCH_ERROR', payload: 'Failed to fetch actions.' });
                }
            }
            catch (err) {
                dispatch({ type: 'FETCH_ERROR', payload: 'Error fetching actions.' });
            }
        };

        fetchActions();
    }, [ category.id ]);

    const addAction = (newAction: Action) => {
        dispatch({ type: 'ADD_ACTION', payload: newAction });
    };

    const deleteAction = async (id: string) => {
        try {
            const response = await api.actions.deleteAction({ id });
            if (response.status) 
                dispatch({ type: 'DELETE_ACTION', payload: id });
            else 
                toast.error('Failed to delete action.');
            
        }
        catch (err) {
            toast.error('Error deleting action.');
        }
    };

    const setModalOpen = (isOpen: boolean) => {
        dispatch({ type: 'TOGGLE_MODAL', payload: isOpen });
    };

    return {
        actions: state.actions,
        loading: state.loading,
        error: state.error,
        isModalOpen: state.isModalOpen,
        addAction,
        deleteAction,
        setModalOpen,
    };
}
