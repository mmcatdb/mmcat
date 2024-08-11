import type { Key } from '../identifiers';

export interface InferenceEdit {
    type: string;
}

export class PrimaryKeyMergeInferenceEdit implements InferenceEdit {
    readonly type: string = 'PrimaryKey';

    constructor(
        readonly primaryKey: Key,
    ) {}
}

export class ReferenceMergeInferenceEdit implements InferenceEdit {
    readonly type: string = 'Reference';

    constructor(
        readonly referenceKey: Key,
        readonly referredKey: Key,
    ) {}
}

export class ClusterInferenceEdit implements InferenceEdit {
    readonly type: string = 'Cluster';

    constructor(
        readonly clusterKeys: Key[],
    ) {}
}

export class RecursionInferenceEdit implements InferenceEdit {
    readonly type: string = 'Recursion';

    constructor(
        readonly pattern: PatternSegment[],
    ) {}
}

export type SaveJobResultPayload = {
    isFinal: false;
    edit: InferenceEdit;
} | {
    isFinal: true;
};

export class PatternSegment {
    constructor(
        readonly nodeName: string,
        readonly direction: string,
    ) {}
}
