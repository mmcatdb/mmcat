export enum SelectionType {
    /** Nothing can be selected. */
    None = 'none',
    /** The user might select any number of nodes/edges he wants. The order doesn't matter. */
    Free = 'free',
    /** The user selects nodes one by one. The order matters. */
    Sequence = 'sequence',
    /** The user selects a path, i.e., a continuous sequence of nodes and edges. */
    Path = 'path',
}

export type GraphSelection = {
    isEmpty: boolean;
};

export * from './FreeSelection';
export * from './SequenceSelection';
export * from './PathSelection';
