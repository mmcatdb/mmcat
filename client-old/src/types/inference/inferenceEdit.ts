import { Key } from '../identifiers';
import type { KeyFromServer } from '../identifiers';
import { PrimaryKeyCandidate, ReferenceCandidate } from './candidates';
import type { LayoutType } from '@/types/inference/layoutType';
import type { SerializedPrimaryKeyCandidate, SerializedReferenceCandidate } from './candidates';

/**
 * Interface representing an inference edit.
 * @interface
 */
export interface InferenceEdit {
    id: number | null;
    isActive: boolean;
    type: string;
}

/**
 * Creates an inference edit object from serialized server data.
 */
export function createInferenceEditFromServer(data: string): InferenceEdit {
    const parsedData = JSON.parse(data) as SerializedInferenceEdit;
    switch (parsedData.type) {
    case 'PrimaryKey':
        return PrimaryKeyMergeInferenceEdit.fromServer(parsedData);
    case 'Reference':
        return ReferenceMergeInferenceEdit.fromServer(parsedData);
    case 'Cluster':
        return ClusterInferenceEdit.fromServer(parsedData);
    case 'Recursion':
        return RecursionInferenceEdit.fromServer(parsedData);
    default:
        throw new Error('Unknown edit type');
    }
}

/**
 * Type representing serialized inference edit data received from the server.
 * @typedef {Object} SerializedInferenceEdit
 * @property {number | null} id - The ID of the edit.
 * @property {boolean} isActive - Whether the edit is active.
 * @property {'PrimaryKey' | 'Reference' | 'Cluster' | 'Recursion'} type - The type of the edit.
 * @property {KeyFromServer} [primaryKey] - The primary key involved in the edit (if applicable).
 * @property {KeyFromServer} [primaryKeyIdentified] - The identified primary key (if applicable).
 * @property {SerializedPrimaryKeyCandidate | SerializedReferenceCandidate} [candidate] - The candidate for the edit (if applicable).
 * @property {KeyFromServer} [referenceKey] - The reference key (if applicable).
 * @property {KeyFromServer} [referredKey] - The referred key (if applicable).
 * @property {KeyFromServer[]} [clusterKeys] - The cluster keys involved (if applicable).
 * @property {SerializedPatternSegment[]} [pattern] - The recursion pattern (if applicable).
 */
export type SerializedInferenceEdit = {
    id: number | null;
    isActive: boolean;
    type: 'PrimaryKey' | 'Reference' | 'Cluster' | 'Recursion';
    primaryKey?: KeyFromServer;
    primaryKeyIdentified?: KeyFromServer;
    candidate?: SerializedPrimaryKeyCandidate | SerializedReferenceCandidate;
    referenceKey?: KeyFromServer;
    referredKey?: KeyFromServer;
    clusterKeys?: KeyFromServer[];
    pattern?: SerializedPatternSegment[];
};

/**
 * Class representing a primary key merge inference edit.
 * @implements {InferenceEdit}
 */
export class PrimaryKeyMergeInferenceEdit implements InferenceEdit {
    readonly type: string = 'PrimaryKey';
    readonly primaryKey?: Key;
    readonly primaryKeyIdentified?: Key;
    readonly candidate?: PrimaryKeyCandidate;
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a `PrimaryKeyMergeInferenceEdit` from either keys or a candidate.
     * @param {Key | PrimaryKeyCandidate} primaryKeyOrCandidate - The primary key or candidate involved in the edit.
     * @param {Key | boolean} primaryKeyIdentifiedOrIsActive - The identified primary key or whether the edit is active.
     * @param {boolean | number | null} [isActiveOrId] - Whether the edit is active or the edit ID.
     * @param {number | null} [id=null] - The ID of the edit.
     */
    constructor(primaryKeyOrCandidate: Key | PrimaryKeyCandidate, primaryKeyIdentifiedOrIsActive: Key | boolean, isActiveOrId?: boolean | number | null, id: number | null = null) {
        if (primaryKeyOrCandidate instanceof Key && primaryKeyIdentifiedOrIsActive instanceof Key) {
            this.primaryKey = primaryKeyOrCandidate;
            this.primaryKeyIdentified = primaryKeyIdentifiedOrIsActive;
            this.isActive = isActiveOrId as boolean;
            this.id = id ?? null;
        } else if (primaryKeyOrCandidate instanceof PrimaryKeyCandidate) {
            this.candidate = primaryKeyOrCandidate;
            this.isActive = primaryKeyIdentifiedOrIsActive as boolean;
            this.id = isActiveOrId as number ?? null;
        } else {
            throw new Error('Invalid constructor arguments for PrimaryKeyMergeInferenceEdit.');
        }
    }

    /**
     * Creates a `PrimaryKeyMergeInferenceEdit` instance from serialized server data.
     * @param {SerializedInferenceEdit} data - The serialized data from the server.
     * @returns {PrimaryKeyMergeInferenceEdit} - The deserialized `PrimaryKeyMergeInferenceEdit` object.
     */
    static fromServer(data: SerializedInferenceEdit): PrimaryKeyMergeInferenceEdit {
        if (data.primaryKey != null && data.primaryKeyIdentified != null) {
            return new PrimaryKeyMergeInferenceEdit(
                Key.fromServer(data.primaryKey),
                Key.fromServer(data.primaryKeyIdentified),
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

/**
 * Class representing a reference merge inference edit.
 * @implements {InferenceEdit}
 */
export class ReferenceMergeInferenceEdit implements InferenceEdit {
    readonly type: string = 'Reference';
    readonly referenceKey?: Key;
    readonly referredKey?: Key;
    readonly candidate?: ReferenceCandidate;
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a `ReferenceMergeInferenceEdit` from either keys or a candidate.
     * @param {Key | ReferenceCandidate} referenceOrCandidate - The reference key or candidate involved in the edit.
     * @param {Key | boolean} referredKeyOrIsActive - The referred key or whether the edit is active.
     * @param {boolean | number | null} [isActiveOrId] - Whether the edit is active or the edit ID.
     * @param {number | null} [id=null] - The ID of the edit.
     */
    constructor(referenceOrCandidate: Key | ReferenceCandidate, referredKeyOrIsActive: Key | boolean, isActiveOrId?: boolean | number | null, id?: number | null) {
        if (referenceOrCandidate instanceof Key && referredKeyOrIsActive instanceof Key) {
            this.referenceKey = referenceOrCandidate;
            this.referredKey = referredKeyOrIsActive;
            this.isActive = isActiveOrId as boolean;
            this.id = id ?? null;
        } else if (referenceOrCandidate instanceof ReferenceCandidate) {
            this.candidate = referenceOrCandidate;
            this.isActive = referredKeyOrIsActive as boolean;
            this.id = isActiveOrId as number ?? null;
        } else {
            throw new Error('Invalid constructor arguments for ReferenceMergeInferenceEdit.');
        }
    }

    /**
     * Creates a `ReferenceMergeInferenceEdit` instance from serialized server data.
     * @param {SerializedInferenceEdit} data - The serialized data from the server.
     * @returns {ReferenceMergeInferenceEdit} - The deserialized `ReferenceMergeInferenceEdit` object.
     */
    static fromServer(data: SerializedInferenceEdit): ReferenceMergeInferenceEdit {
        if (data.referenceKey != null && data.referredKey != null) {
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

/**
 * Class representing a cluster inference edit.
 * @implements {InferenceEdit}
 */
export class ClusterInferenceEdit implements InferenceEdit {
    readonly type: string = 'Cluster';
    readonly clusterKeys: Key[];
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a `ClusterInferenceEdit`.
     * @param {Key[]} clusterKeys - The keys involved in the cluster.
     * @param {boolean} isActive - Whether the edit is active.
     * @param {number | null} [id=null] - The ID of the edit.
     */
    constructor(clusterKeys: Key[], isActive: boolean, id: number | null = null) {
        this.clusterKeys = clusterKeys;
        this.isActive = isActive;
        this.id = id;
    }

    /**
     * Creates a `ClusterInferenceEdit` instance from serialized server data.
     * @param {SerializedInferenceEdit} data - The serialized data from the server.
     * @returns {ClusterInferenceEdit} - The deserialized `ClusterInferenceEdit` object.
     */
    static fromServer(data: SerializedInferenceEdit): ClusterInferenceEdit {
        return new ClusterInferenceEdit(
            data.clusterKeys!.map(Key.fromServer),
            data.isActive,
            data.id,
        );
    }
}

/**
 * Class representing a recursion inference edit.
 * @implements {InferenceEdit}
 */
export class RecursionInferenceEdit implements InferenceEdit {
    readonly type: string = 'Recursion';
    readonly pattern: PatternSegment[];
    public isActive: boolean;
    public id: number | null;

    /**
     * Constructs a `RecursionInferenceEdit`.
     * @param {PatternSegment[]} pattern - The recursion pattern.
     * @param {boolean} isActive - Whether the edit is active.
     * @param {number | null} [id=null] - The ID of the edit.
     */
    constructor(pattern: PatternSegment[], isActive: boolean, id: number | null = null) {
        this.pattern = pattern;
        this.isActive = isActive;
        this.id = id;
    }

    /**
     * Creates a `RecursionInferenceEdit` instance from serialized server data.
     * @param {SerializedInferenceEdit} data - The serialized data from the server.
     * @returns {RecursionInferenceEdit} - The deserialized `RecursionInferenceEdit` object.
     */
    static fromServer(data: SerializedInferenceEdit): RecursionInferenceEdit {
        return new RecursionInferenceEdit(
            data.pattern!.map(segment => new PatternSegment(segment.nodeName, segment.direction)),
            data.isActive,
            data.id,
        );
    }
}

/**
 * Class representing a segment of a recursion pattern.
 */
export class PatternSegment {
    /**
     * Constructs a `PatternSegment`.
     * @param {string} nodeName - The name of the node in the pattern.
     * @param {string} direction - The direction of the recursion ('->', '<-', or '').
     */
    constructor(
        readonly nodeName: string,
        readonly direction: string,
    ) {}

    /**
     * Creates a `PatternSegment` instance from serialized server data.
     * @param {SerializedPatternSegment} data - The serialized pattern segment data from the server.
     * @returns {PatternSegment} - The deserialized `PatternSegment` object.
     */
    static fromServer(data: SerializedPatternSegment): PatternSegment {
        return new PatternSegment(data.nodeName, data.direction);
    }
}

/**
 * Type representing a serialized recursion pattern segment.
 * @typedef {Object} SerializedPatternSegment
 * @property {string} nodeName - The name of the node in the pattern.
 * @property {string} direction - The direction of the recursion.
 */
export type SerializedPatternSegment = {
    nodeName: string;
    direction: string;
};

/**
 * Type representing the payload when saving a job result.
 * @typedef {Object} SaveJobResultPayload
 * @property {boolean | null} isFinal - Whether the job result is final.
 * @property {InferenceEdit | null} edit - The associated inference edit, if any.
 * @property {LayoutType | null} layoutType - The layout type used.
 */
export type SaveJobResultPayload = {
    isFinal: boolean | null;
    edit: InferenceEdit | null;
    layoutType: LayoutType | null;
};
