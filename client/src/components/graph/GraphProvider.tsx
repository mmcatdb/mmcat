import { type ReactNode, useEffect, useMemo, useState, type Dispatch, useRef } from 'react';
import { createInitialGraphState, defaultGraphOptions, GraphEngine, type GraphOptions, type Graph, type ReactiveGraphState, type GraphEvent } from './graphEngine';
import { graphContext } from './graphHooks';

type GraphProviderProps = {
    /** The public graph that the graph exposes. Its updates are reflected in the graph display. */
    graph: Graph;
    /** Event handler for things like position change or select. */
    dispatch: Dispatch<GraphEvent>;
    /** User preferences. The graph engine is restarted whenever they change, so make sure they are constant or at least memoized! */
    options?: GraphOptions;
    children: ReactNode;
};

export function GraphProvider({ graph, dispatch, options, children }: GraphProviderProps) {
    const { state, engine } = useGraphEngine(graph, dispatch, options);

    const context = useMemo(() => ({ engine, state }), [ engine, state ]);

    return (
        <graphContext.Provider value={context}>
            {children}
        </graphContext.Provider>
    );
}

function useGraphEngine(graph: Graph, dispatch: Dispatch<GraphEvent>, options?: GraphOptions) {
    const [ state, setState ] = useState<ReactiveGraphState>(() => createInitialGraphState(graph));
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
