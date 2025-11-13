import { Signature, type SignatureResponse } from '../identifiers';
import type { SchemaMorphism } from '../schema';
import type { InstanceCategory } from './InstanceCategory';
import type { DomainRow, InstanceObjex } from './InstanceObjex';

export type InstanceMorphismResponse = {
    signature: SignatureResponse;
    mappings: MappingRowResponse[];
};

export class InstanceMorphism {
    private constructor(
        readonly schema: SchemaMorphism,
        readonly dom: InstanceObjex,
        readonly cod: InstanceObjex,
        readonly mappings: MappingRow[],
    ) {}

    static fromResponse(input: InstanceMorphismResponse, instance: InstanceCategory): InstanceMorphism | undefined {
        const signature = Signature.fromResponse(input.signature);
        const schemaMorphism = instance.schema.getMorphism(signature).schema;
        if (!schemaMorphism)
            return;

        const dom = instance.objexes.get(schemaMorphism.domKey);
        const cod = instance.objexes.get(schemaMorphism.codKey);
        if (!dom || !cod)
            return;

        const mappings: MappingRow[] = [];
        input.mappings.forEach(mapping => {
            const domRow = dom.idToRow.get(mapping.dom);
            const codRow = cod.idToRow.get(mapping.cod);
            if (domRow && codRow)
                mappings.push(new MappingRow(domRow, codRow));
        });

        return new InstanceMorphism(
            schemaMorphism,
            dom,
            cod,
            mappings,
        );
    }
}

export type MappingRowResponse = {
    dom: number;
    cod: number;
};

export class MappingRow {
    constructor(
        readonly dom: DomainRow,
        readonly cod: DomainRow,
    ) {}
}
