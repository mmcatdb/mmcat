import { Key, Signature, SignatureId, type KeyFromServer, type SignatureFromServer, type SignatureIdFromServer } from "../identifiers";
import { DomainRow, type DomainRowFromServer } from "./InstanceObject";

export class InstanceMorphism {
    private constructor(
        readonly signature: Signature,
        readonly domKey: Key,
        readonly codKey: Key,
        readonly domSuperId: SignatureId,
        readonly codSuperId: SignatureId,
        readonly mappings: MappingRow[]
    ) {}

    static fromServer(input: InstanceMorphismFromServer): InstanceMorphism {
        return new InstanceMorphism(
            Signature.fromServer(input.signature),
            Key.fromServer(input.domKey),
            Key.fromServer(input.codKey),
            SignatureId.fromServer(input.domSuperId),
            SignatureId.fromServer(input.codSuperId),
            input.mappings.map(MappingRow.fromServer)
        );
    }

    get showDomTechnicalIds(): boolean {
        return !!this.mappings.find(mapping => mapping.domRow.technicalIds.size > 0);
    }

    get showCodTechnicalIds(): boolean {
        return !!this.mappings.find(mapping => mapping.codRow.technicalIds.size > 0);
    }
}

export type InstanceMorphismFromServer = {
    signature: SignatureFromServer;
    domKey: KeyFromServer;
    codKey: KeyFromServer;
    domSuperId: SignatureIdFromServer;
    codSuperId: SignatureIdFromServer;
    mappings: MappingRowFromServer[];
};

export class MappingRow {
    private constructor(
        readonly domRow: DomainRow,
        readonly codRow: DomainRow
    ) {}

    static fromServer(input: MappingRowFromServer) {
        return new MappingRow(
            DomainRow.fromServer(input.domRow),
            DomainRow.fromServer(input.codRow),
        );
    }
}

export type MappingRowFromServer = {
    domRow: DomainRowFromServer;
    codRow: DomainRowFromServer;
};
