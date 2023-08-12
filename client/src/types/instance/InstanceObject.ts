import { ComparableMap } from '@/utils/ComparableMap';
import { Key, Signature, SignatureId, type KeyFromServer, type SignatureFromServer, type SignatureIdFromServer } from '../identifiers';

export class InstanceObject {
    private constructor(
        readonly key: Key,
        readonly superId: SignatureId,
        readonly rows: DomainRow[],
    ) {}

    static fromServer(input: InstanceObjectFromServer): InstanceObject {
        return new InstanceObject(
            Key.fromServer(input.key),
            SignatureId.fromServer(input.superId),
            input.rows.map(DomainRow.fromServer),
        );
    }
}

export type InstanceObjectFromServer = {
    key: KeyFromServer;
    superId: SignatureIdFromServer;
    rows: DomainRowFromServer[];
};

export class DomainRow {
    private constructor(
        readonly superId: SuperIdWithValues,
        readonly technicalIds: Set<string>,
    ) {}

    static fromServer(input: DomainRowFromServer) {
        return new DomainRow(
            SuperIdWithValues.fromServer(input.superId),
            new Set(input.technicalIds),
        );
    }

    get technicalIdsString(): string {
        return [ ...this.technicalIds ].join(', ');
    }
}

export type DomainRowFromServer = {
    superId: SuperIdWithValuesFromServer;
    technicalIds: string[];
};

export class SuperIdWithValues {
    private constructor(
        readonly tuples: ComparableMap<Signature, string, string>,
    ) {}

    static fromServer(input: SuperIdWithValuesFromServer): SuperIdWithValues {
        const tuples = new ComparableMap<Signature, string, string>(signature => signature.value);
        input.forEach(tuple => tuples.set(Signature.fromServer(tuple.signature), tuple.value));

        return new SuperIdWithValues(tuples);
    }
}

type SingatureValueTupleFromServer = {
    signature: SignatureFromServer;
    value: string;
};

export type SuperIdWithValuesFromServer = SingatureValueTupleFromServer[];
