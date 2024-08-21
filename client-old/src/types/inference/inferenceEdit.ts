import { Key } from '../identifiers';
import type { KeyFromServer } from '../identifiers';
import { PrimaryKeyCandidate, ReferenceCandidate } from './candidates';
import { LayoutType } from '@/types/inference/layoutType';
import type { SerializedPrimaryKeyCandidate, SerializedReferenceCandidate } from './candidates';

export interface InferenceEdit {
    id: number | null;
    isActive: boolean;
    type: string;
}

export function createInferenceEditFromServer(data: SerializedInferenceEdit): InferenceEdit {
    switch (data.type) {
    case 'PrimaryKey':
        return PrimaryKeyMergeInferenceEdit.fromServer(data);
    case 'Reference':
        return ReferenceMergeInferenceEdit.fromServer(data);
    case 'Cluster':
        return ClusterInferenceEdit.fromServer(data);
    case 'Recursion':
        return RecursionInferenceEdit.fromServer(data);
    default:
        throw new Error('Unknown edit type');
    }
}

export type SerializedInferenceEdit = {
    id: number | null;
    isActive: boolean;
    type: 'PrimaryKey' | 'Reference' | 'Cluster' | 'Recursion';
    primaryKey?: KeyFromServer;
    candidate?: SerializedPrimaryKeyCandidate | SerializedReferenceCandidate;
    referenceKey?: KeyFromServer;
    referredKey?: KeyFromServer;
    clusterKeys?: KeyFromServer[];
    pattern?: SerializedPatternSegment[];
};

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

    static fromServer(data: SerializedInferenceEdit): PrimaryKeyMergeInferenceEdit {
        if (data.primaryKey != null) {
            return new PrimaryKeyMergeInferenceEdit(
                Key.fromServer(data.primaryKey),
                data.isActive,
                data.id,
            );
        } else if (data.candidate != null) {
            return new PrimaryKeyMergeInferenceEdit(
                PrimaryKeyCandidate.fromServer(data.candidate as SerializedPrimaryKeyCandidate),
                data.isActive,
                data.id,
            );
        }
        throw new Error('Invalid server data for PrimaryKeyMergeInferenceEdit');
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

    static fromServer(data: SerializedInferenceEdit): ReferenceMergeInferenceEdit {
        console.log('data referenceKey: ', data.referenceKey);
        if (data.referenceKey != null && data.referredKey != null) { // tuhle logiku dat vsude s tim null
            return new ReferenceMergeInferenceEdit(
                Key.fromServer(data.referenceKey),
                Key.fromServer(data.referredKey),
                data.isActive,
                data.id,
            );
        } else if (data.candidate != null) {
            return new ReferenceMergeInferenceEdit(
                ReferenceCandidate.fromServer(data.candidate as SerializedReferenceCandidate),
                data.isActive,
                data.id,
            );
        }
        throw new Error('Invalid server data for ReferenceMergeInferenceEdit');
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

    static fromServer(data: SerializedInferenceEdit): ClusterInferenceEdit {
        return new ClusterInferenceEdit(
            data.clusterKeys!.map(Key.fromServer),
            data.isActive,
            data.id,
        );
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

    static fromServer(data: SerializedInferenceEdit): RecursionInferenceEdit {
        return new RecursionInferenceEdit(
            data.pattern!.map(segment => new PatternSegment(segment.nodeName, segment.direction)),
            data.isActive,
            data.id,
        );
    }
}

export class PatternSegment {
    constructor(
        readonly nodeName: string,
        readonly direction: string,
    ) {}

    static fromServer(data: SerializedPatternSegment): PatternSegment {
        return new PatternSegment(data.nodeName, data.direction);
    }
}

export type SerializedPatternSegment = {
    nodeName: string;
    direction: string;
};

export type SaveJobResultPayload = {
    isFinal: boolean | null;
    edit: InferenceEdit | null;
    layoutType: LayoutType | null;
};

