/**
 * Class representing a collection of primary key and reference candidates.
 */
export class Candidates {
    constructor(
        public pkCandidates: PrimaryKeyCandidate[],
        public refCandidates: ReferenceCandidate[],
    ) {}

    static fromServer(input: SerializedCandidates): Candidates {
        const pkCandidates = input.pkCandidates.map(PrimaryKeyCandidate.fromServer);
        const refCandidates = input.refCandidates.map(ReferenceCandidate.fromServer);

        return new Candidates(pkCandidates, refCandidates);
    }
}

/**
 * Type representing the structure of serialized candidates received from the server.
 */
export type SerializedCandidates = {
    /** Serialized array of primary key candidates. */
    pkCandidates: SerializedPrimaryKeyCandidate[];
    /** Serialized array of reference candidates. */
    refCandidates: SerializedReferenceCandidate[];
};

/**
 * Type representing a serialized primary key candidate.
 */
export type SerializedPrimaryKeyCandidate = {
    /** The type of the primary key candidate. */
    type: string;
    /** The hierarchical name of the candidate. */
    hierarchicalName: string;
    /** Whether the candidate is selected. */
    isSelected: boolean;
};

/**
 * Type representing a serialized reference candidate.
 */
export type SerializedReferenceCandidate = {
    /** The type of the reference candidate. */
    type: string;
    /** The referred objex name. */
    referred: string;
    /** The referencing objex name. */
    referencing: string;
    /** Whether the reference is weak. */
    isWeak: boolean;
    /** Whether the candidate is selected. */
    isSelected: boolean;
};

/**
 * Class representing a primary key candidate.
 */
export class PrimaryKeyCandidate {
    constructor(
        public type: string,
        public hierarchicalName: string,
        public isSelected: boolean,
    ) {}

    static fromServer(input: SerializedPrimaryKeyCandidate): PrimaryKeyCandidate {
        return new PrimaryKeyCandidate(
            input.type,
            input.hierarchicalName,
            input.isSelected,
        );
    }
}

/**
 * Class representing a reference candidate.
 */
export class ReferenceCandidate {
    constructor(
        public type: string,
        public referred: string,
        public referencing: string,
        public isWeak: boolean,
        public isSelected: boolean,
    ) {}

    static fromServer(input: SerializedReferenceCandidate): ReferenceCandidate {
        return new ReferenceCandidate(
            input.type,
            input.referred,
            input.referencing,
            input.isWeak,
            input.isSelected,
        );
    }
}
