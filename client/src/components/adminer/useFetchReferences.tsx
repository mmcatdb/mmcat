import { useState, useEffect } from 'react';
import type { AdminerState } from '@/types/adminer/Reducer';
import type { AdminerReferences } from '@/types/adminer/AdminerReferences';
import { api } from '@/api';

export function useFetchReferences( state: AdminerState ) {
    const [ references, setReferences ] = useState<AdminerReferences | undefined>();
    const [ loading, setLoading ] = useState<boolean>(true);

    useEffect(() => {
        (async () => {
            setLoading(true);

            const response = await api.adminer.getReferences({ datasourceId: state.datasourceId!, kindName: state.kindName! });

            if (!response.status)
                setReferences(undefined);
            else
                setReferences(response.data);

            setLoading(false);
        })();
    }, [ state.datasourceId, state.kindName ]);

    return { references, referencesLoading: loading };
}
