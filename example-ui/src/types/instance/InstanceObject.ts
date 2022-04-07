import { Key, Signature, type KeyFromServer, type SignatureFromServer } from "../identifiers";

export class InstanceObject {
    key!: Key;
    columns!: Signature[];
    rows!: string[][];

    private constructor() {}

    static fromServer(input: InstanceObjectFromServer): InstanceObject {
        const output = new InstanceObject;

        output.key = Key.fromServer(input.key);
        output.columns = input.columns.map(signature => Signature.fromServer(signature));
        output.rows = input.rows;

        return output;
    }
}

export type InstanceObjectFromServer = {
    key: KeyFromServer;
    columns: SignatureFromServer[];
    rows: string[][];
}
