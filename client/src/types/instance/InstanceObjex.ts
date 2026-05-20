import { ComparableMap } from '@/types/utils/ComparableMap';
import { Signature, type KeyResponse, type SignatureResponse, Key } from '../identifiers';
import type { Category, SchemaObjex } from '../schema';

export type InstanceObjexResponse = {
    key: KeyResponse;
    rows: DomainRowResponse[];
};

export class InstanceObjex {
    private constructor(
        readonly schema: SchemaObjex,
        readonly rows: DomainRow[],
        readonly idToRow: Map<number, DomainRow>,
    ) {}

    static fromResponse(input: InstanceObjexResponse, schema: Category): InstanceObjex | undefined {
        const key = Key.fromResponse(input.key);
        const schemaObjex = schema.getObjex(key).schema;

        const idToRow = new Map<number, DomainRow>();
        const rows = input.rows.map(inputRow => {
            const row = DomainRow.fromResponse(inputRow);
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

type DomainRowResponse = {
    id: number;
    values: SuperIdValuesResponse;
    pendingReferences: Signature[];
};

export class DomainRow {
    private constructor(
        readonly id: number,
        readonly values: SuperIdValues,
    ) {}

    static fromResponse(input: DomainRowResponse) {
        return new DomainRow(
            input.id,
            SuperIdValues.fromResponse(input.values),
        );
    }
}

type SingatureValueTupleResponse = {
    signature: SignatureResponse;
    value: string;
};

type SuperIdValuesResponse = SingatureValueTupleResponse[];

export class SuperIdValues {
    private constructor(
        readonly tuples: ComparableMap<Signature, string, string>,
    ) {}

    static fromResponse(input: SuperIdValuesResponse): SuperIdValues {
        const tuples = new ComparableMap<Signature, string, string>(signature => signature.value);
        input.forEach(tuple => tuples.set(Signature.fromResponse(tuple.signature), tuple.value));

        return new SuperIdValues(tuples);
    }
}
