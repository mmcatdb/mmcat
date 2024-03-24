import { Signature, type SignatureFromServer } from '../identifiers';
import type { SchemaMorphism } from '../schema';
import type { InstanceCategory } from './InstanceCategory';
import type { DomainRow, InstanceObject } from './InstanceObject';

export type InstanceMorphismFromServer = {
    signature: SignatureFromServer;
    mappings: MappingRowFromServer[];
};

export class InstanceMorphism {
    private constructor(
        readonly schema: SchemaMorphism,
        readonly dom: InstanceObject,
        readonly cod: InstanceObject,
        readonly mappings: MappingRow[],
    ) {}

    static fromServer(input: InstanceMorphismFromServer, instance: InstanceCategory): InstanceMorphism | undefined {
        const signature = Signature.fromServer(input.signature);
        const morphism = instance.schema.getMorphism(signature).current;
        if (!morphism)
            return;

        const dom = instance.objects.get(morphism.domKey);
        const cod = instance.objects.get(morphism.codKey);
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

    get showDomTechnicalIds(): boolean {
        return !!this.mappings.find(mapping => mapping.dom.technicalIds.size > 0);
    }

    get showCodTechnicalIds(): boolean {
        return !!this.mappings.find(mapping => mapping.cod.technicalIds.size > 0);
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
