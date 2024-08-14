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

    toServer(): SerializedCandidates {
        return {
            pkCandidates: this.pkCandidates.map(candidate => candidate.toServer()),
            refCandidates: this.refCandidates.map(candidate => candidate.toServer()),
        };
    }
}

export type SerializedCandidates = {
    pkCandidates: SerializedPrimaryKeyCandidate[];
    refCandidates: SerializedReferenceCandidate[];
};

export type SerializedPrimaryKeyCandidate = {
    type: string;
    hierarchicalName: string;
    selected: boolean;
};

export type SerializedReferenceCandidate = {
    type: string;
    referred: string;
    referencing: string;
    weak: boolean;
    selected: boolean;
};

export class PrimaryKeyCandidate {
    constructor(
        public type: string,
        public hierarchicalName: string,
        public selected: boolean,
    ) {}

    static fromServer(input: SerializedPrimaryKeyCandidate): PrimaryKeyCandidate {
        return new PrimaryKeyCandidate(
            input.type,
            input.hierarchicalName,
            input.selected
        );
    }
/*
    toServer(): SerializedPrimaryKeyCandidate {
        return {
            type: this.type,
            hierarchicalName: this.hierarchicalName,
            selected: this.selected,
        };
    }*/
}

export class ReferenceCandidate {
    constructor(
        public type: string,
        public referred: string,
        public referencing: string,
        public weak: boolean,
        public selected: boolean,
    ) {}

    static fromServer(input: SerializedReferenceCandidate): ReferenceCandidate {
        return new ReferenceCandidate(
            input.type,
            input.referred,
            input.referencing,
            input.weak,
            input.selected
        );
    }
/*
    toServer(): SerializedReferenceCandidate {
        return {
            type: this.type,
            referred: this.referred,
            referencing: this.referencing,
            weak: this.weak,
            selected: this.selected,
        };
    }*/
}
