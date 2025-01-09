import { type ReactNode, useEffect, useMemo, useState, type Dispatch, useRef } from 'react';
import { createInitialGraphState, defaultGraphOptions, type GraphAction, GraphEngine, type GraphOptions, type GraphInput, type ReactiveGraphState } from './graphEngine';
import { graphContext } from './graphHooks';

type GraphProviderProps = Readonly<{
    /** The public graph that the graph exposes. Its updates are reflected in the graph display. */
    graph: GraphInput;
    /** Event handler for things like position change or select. */
    dispatch: Dispatch<GraphAction>;
    /** User preferences. The graph engine is restarted whenever they change, so make sure they are constant or at least memoized! */
    options?: GraphOptions;
    children: ReactNode;
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

function useGraphEngine(graph: GraphInput, dispatch: Dispatch<GraphAction>, options?: GraphOptions) {
    const [ state, setState ] = useState<ReactiveGraphState>(() => createInitialGraphState(graph, options));
    const engine = useMemo(() => new GraphEngine(graph, dispatch, state, setState, { ...defaultGraphOptions, ...options }), [ options ]);

    useEffect(() => {
        return engine.setup();
    }, [ engine ]);

    const cache = useRef(graph);
    if (cache.current !== graph) {
        cache.current = graph;
        engine.update(graph);
    }

    return { state, engine } as const;
}
