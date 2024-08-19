import { Key } from '../identifiers';
import type { KeyFromServer } from '../identifiers';
import { PrimaryKeyCandidate, ReferenceCandidate } from './candidates';
import { LayoutType } from '@/types/inference/layoutType';

export interface InferenceEdit {
    id: number | null;
    isActive: boolean;
    type: string;
}

export class PrimaryKeyMergeInferenceEdit implements InferenceEdit {
    readonly type: string = 'PrimaryKey';
    readonly primaryKey?: Key;
    readonly candidate?: PrimaryKeyCandidate;
    public isActive: boolean;
    public id: number | null;

    constructor(primaryKey: Key, isActive: boolean, id?: number | null);
    constructor(candidate: PrimaryKeyCandidate, isActive: boolean, id?: number | null);

    constructor(primaryKeyOrCandidate: Key | PrimaryKeyCandidate, isActive: boolean, id: number | null = null) {
        if (primaryKeyOrCandidate instanceof Key) 
            this.primaryKey = primaryKeyOrCandidate;
        else 
            this.candidate = primaryKeyOrCandidate;
        
        this.isActive = isActive;
        this.id = id;
    }
}

export class ReferenceMergeInferenceEdit implements InferenceEdit {
    readonly type: string = 'Reference';
    readonly referenceKey?: Key;
    readonly referredKey?: Key;
    readonly candidate?: ReferenceCandidate;
    public isActive: boolean;
    public id: number | null;

    constructor(reference: Key, referredKey: Key, isActive: boolean, id?: number | null);
    constructor(candidate: ReferenceCandidate, isActive: boolean, id?: number | null);

    constructor(referenceOrCandidate: Key | ReferenceCandidate, referredKeyOrIsActive: Key | boolean, isActiveOrId?: boolean | number | null, id?: number | null) {
        if (referenceOrCandidate instanceof Key && referredKeyOrIsActive instanceof Key) {
            this.referenceKey = referenceOrCandidate;
            this.referredKey = referredKeyOrIsActive;
            this.isActive = isActiveOrId as boolean;
            this.id = id ?? null;
        } else if (referenceOrCandidate instanceof ReferenceCandidate) {
            this.candidate = referenceOrCandidate;
            this.isActive = referredKeyOrIsActive as boolean;
            this.id =  isActiveOrId as number ?? null;
        } else {
            throw new Error('Invalid constructor arguments for ReferenceMergeInferenceEdit.');
        }
    }
}

export class ClusterInferenceEdit implements InferenceEdit {
    readonly type: string = 'Cluster';
    readonly clusterKeys: Key[];
    public isActive: boolean;
    public id: number | null;

    constructor(clusterKeys: Key[], isActive: boolean, id: number | null = null) {
        this.clusterKeys = clusterKeys;
        this.isActive = isActive;
        this.id = id;
    }
}

export class RecursionInferenceEdit implements InferenceEdit {
    readonly type: string = 'Recursion';
    readonly pattern: PatternSegment[];
    public isActive: boolean;
    public id: number | null;

    constructor(pattern: PatternSegment[], isActive: boolean, id: number | null = null) {
        this.pattern = pattern;
        this.isActive = isActive;
        this.id = id;
    }
}

export function createInferenceEditFromServer(data: any): InferenceEdit {
    switch (data.type) {
    case 'PrimaryKey':
        return new PrimaryKeyMergeInferenceEdit(
            Key.fromServer(data.primaryKey), 
            data.isActive, 
            data.id,
        );
    case 'Reference':
        return new ReferenceMergeInferenceEdit(
            Key.fromServer(data.referenceKey),
            Key.fromServer(data.referredKey),
            data.isActive,
            data.id,
        );
    case 'Cluster':
        return new ClusterInferenceEdit(
            data.clusterKeys.map((key: KeyFromServer) => Key.fromServer(key)),
            data.isActive,
            data.id,
        );
    case 'Recursion':
        return new RecursionInferenceEdit(
            data.pattern.map((segment: any) => new PatternSegment(segment.nodeName, segment.direction)),
            data.isActive,
            data.id,
        );
    default:
        throw new Error('Unknown edit type');
    }
}

export type SaveJobResultPayload = {
    isFinal: boolean | null;
    edit: InferenceEdit | null;
    layoutType: LayoutType | null;
};


export class PatternSegment {
    constructor(
        readonly nodeName: string,
        readonly direction: string,
    ) {}
}
