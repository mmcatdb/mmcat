import { Signature, type SignatureFromServer } from '../identifiers';
import type { SchemaMorphism } from '../schema';
import type { InstanceCategory } from './InstanceCategory';
import type { DomainRow, InstanceObjex } from './InstanceObjex';

export type InstanceMorphismFromServer = {
    signature: SignatureFromServer;
    mappings: MappingRowFromServer[];
};

export class InstanceMorphism {
    private constructor(
        readonly schema: SchemaMorphism,
        readonly dom: InstanceObjex,
        readonly cod: InstanceObjex,
        readonly mappings: MappingRow[],
    ) {}

    static fromServer(input: InstanceMorphismFromServer, instance: InstanceCategory): InstanceMorphism | undefined {
        const signature = Signature.fromServer(input.signature);
        const morphism = instance.schema.getMorphism(signature).current;
        if (!morphism)
            return;

        const dom = instance.objexes.get(morphism.domKey);
        const cod = instance.objexes.get(morphism.codKey);
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
            morphism,
            dom,
            cod,
            mappings,
        );
    }

    get showDomTechnicalId(): boolean {
        return !!this.mappings.find(mapping => mapping.dom.technicalId !== undefined);
    }

    get showCodTechnicalId(): boolean {
        return !!this.mappings.find(mapping => mapping.cod.technicalId !== undefined);
    }
}

export type MappingRowFromServer = {
    dom: number;
    cod: number;
};

export class MappingRow {
    public constructor(
        readonly dom: DomainRow,
        readonly cod: DomainRow,
    ) {}
}
