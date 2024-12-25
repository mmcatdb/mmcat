import { useEffect, useMemo, useState, type Dispatch } from 'react';
import { createInitialGraphState, defaultGraphOptions, type GraphAction, GraphEngine, type GraphOptions, type GraphValue, type ReactiveGraphState } from './graphEngine';
import { graphContext } from './graphHooks';

type GraphProviderProps = Readonly<{
    /** The public graph that the graph exposes. Its updates are reflected in the graph display. */
    graph: GraphValue;
    /** Event handler for things like position change or select. */
    dispatch: Dispatch<GraphAction>;
    /** User preferences. The graph engine is restarted whenever they change, so make sure they are constant or at least memoized! */
    options?: GraphOptions;
    children: React.ReactNode;
}>;

export function GraphProvider({ graph, dispatch, options, children }: GraphProviderProps) {
    const { state, engine } = useGraphEngine(graph, dispatch, options);

    const context = useMemo(() => ({ engine, state }), [ engine, state ]);

    return (
        <graphContext.Provider value={context}>
            {children}
        </graphContext.Provider>
    );
}

function useGraphEngine(value: GraphValue, dispatch: Dispatch<GraphAction>, options?: GraphOptions) {
    const [ state, setState ] = useState<ReactiveGraphState>(() => createInitialGraphState(value, options));
    const engine = useMemo(() => new GraphEngine(value, dispatch, state, setState, { ...defaultGraphOptions, ...options }), [ options ]);

    useEffect(() => {
        return engine.setup();
    }, [ engine ]);

    return { state, engine } as const;
}
