import { ComparableMap } from '@/utils/ComparableMap';
import { Signature, type KeyFromServer, type SignatureFromServer, Key } from '../identifiers';
import type { Category, SchemaObjex } from '../schema';

export type InstanceObjexFromServer = {
    key: KeyFromServer;
    rows: DomainRowFromServer[];
};

export class InstanceObjex {
    private constructor(
        readonly schema: SchemaObjex,
        readonly rows: DomainRow[],
        readonly idToRow: Map<number, DomainRow>,
    ) {}

    static fromServer(input: InstanceObjexFromServer, schema: Category): InstanceObjex | undefined {
        const key = Key.fromServer(input.key);
        const object = schema.getObjex(key).current;
        if (!object)
            return;

        const idToRow = new Map<number, DomainRow>();
        const rows = input.rows.map(inputRow => {
            const row = DomainRow.fromServer(inputRow);
            idToRow.set(inputRow.id, row);
            return row;
        });

        return new InstanceObjex(
            object,
            rows,
            idToRow,
        );
    }
}

export type DomainRowFromServer = {
    id: number;
    superId: SuperIdWithValuesFromServer;
    technicalIds: string[];
    pendingReferences: Signature[];
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

type SingatureValueTupleFromServer = {
    signature: SignatureFromServer;
    value: string;
};

type SuperIdWithValuesFromServer = SingatureValueTupleFromServer[];

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
