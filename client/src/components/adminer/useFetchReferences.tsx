import { useState, useEffect } from 'react';
import type { AdminerState } from '@/types/adminer/Reducer';
import type { AdminerReferences } from '@/types/adminer/AdminerReferences';
import { api } from '@/api';

export function useFetchReferences( state: AdminerState ) {
    const [ references, setReferences ] = useState<AdminerReferences | undefined>();
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ error, setError ] = useState<string | undefined>();

    useEffect(() => {
        (async () => {
            setLoading(true);
            setError(undefined);

            const response = await api.adminer.getReferences({ datasourceId: state.datasourceId!, kindName: state.kindName! });

            if (!response.status)
                setError('Failed to fetch references.');
            else
                setReferences(response.data);

            setLoading(false);
        })();
    }, [ state.datasourceId, state.kindName ]);

    return { references, refLoading: loading, refError: error };
}
