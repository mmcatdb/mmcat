import { ComparableMap } from '@/types/utils/ComparableMap';
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
        const schemaObjex = schema.getObjex(key).schema;

        const idToRow = new Map<number, DomainRow>();
        const rows = input.rows.map(inputRow => {
            const row = DomainRow.fromServer(inputRow);
            idToRow.set(inputRow.id, row);
            return row;
        });

        return new InstanceObjex(
            schemaObjex,
            rows,
            idToRow,
        );
    }
}

type DomainRowFromServer = {
    id: number;
    values: SuperIdValuesFromServer;
    technicalId: number | null;
    pendingReferences: Signature[];
};

export class DomainRow {
    private constructor(
        readonly values: SuperIdValues,
        readonly technicalId: number | undefined,
    ) {}

    static fromServer(input: DomainRowFromServer) {
        return new DomainRow(
            SuperIdValues.fromServer(input.values),
            input.technicalId ?? undefined,
        );
    }
}

type SingatureValueTupleFromServer = {
    signature: SignatureFromServer;
    value: string;
};

type SuperIdValuesFromServer = SingatureValueTupleFromServer[];

export class SuperIdValues {
    private constructor(
        readonly tuples: ComparableMap<Signature, string, string>,
    ) {}

    static fromServer(input: SuperIdValuesFromServer): SuperIdValues {
        const tuples = new ComparableMap<Signature, string, string>(signature => signature.value);
        input.forEach(tuple => tuples.set(Signature.fromServer(tuple.signature), tuple.value));

        return new SuperIdValues(tuples);
    }
}
