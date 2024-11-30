import { useState, useEffect } from 'react';
import { type AdminerState } from '@/types/adminer/Reducer';
import { type AdminerReference } from '@/types/adminer/AdminerReference';

export function useFetchReferences( state: AdminerState ) {
    const [ references, setReferences ] = useState<AdminerReference | undefined>();
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | undefined>();

    useEffect(() => {
        (() => {
            setLoading(true);
            setError(undefined);
            setReferences(undefined);
            setLoading(false);
        })();
    }, [ state.datasource, state.kind ]);

    return { references, loading, error };
}
