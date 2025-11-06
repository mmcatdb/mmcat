import { type DatasourceResponse, type DatasourceType, Datasource } from '../Datasource';
import { ComplexProperty, Mapping, type MappingResponse, type ParentProperty } from '@/types/mapping';
import { DynamicName, type Key, type Signature } from '../identifiers';
import { type Objex, type Morphism } from '@/types/schema';
import { type ComparableMap } from '@/types/utils/ComparableMap';
import { ComparableSet } from '@/types/utils/ComparableSet';
import { type Category } from '../schema';

export type LogicalModel = {
    datasource: DatasourceResponse;
    mappings: Mapping[];
};

export function logicalModelsFromResponse(datasources: DatasourceResponse[], mappings: MappingResponse[]): LogicalModel[] {
    const allDatasources = datasources.map(Datasource.fromResponse);
    const allMappings = mappings.map(Mapping.fromResponse);

    return allDatasources.map(datasource => ({
        datasource,
        mappings: allMappings.filter(mapping => mapping.datasourceId === datasource.id),
    }));
}

// TODO Not used right now, use it in the graph.

/**
 * Used for highlighting logical models in the graph.
 */
export class CategoryLogicalModels {
    readonly groups: GroupData[];

    private constructor(
        readonly category: Category,
        readonly logicalModels: LogicalModel[],
    ) {
        this.groups = createGroups(logicalModels, category);
    }
}

type GroupMapping = {
    mapping: Mapping;
    root: Objex;
    properties: Objex[];
    groupId: string;
};

type GroupData = {
    id: string;
    logicalModel: LogicalModel;
    mappings: GroupMapping[];
};

type CategoryContext = {
    objexes: ComparableMap<Key, number, Objex>;
    morphisms: ComparableMap<Signature, string, Morphism>;
};

function createGroups(logicalModels: LogicalModel[], context: CategoryContext): GroupData[] {
    const typeIndices = new Map<DatasourceType, number>();

    return logicalModels.map(logicalModel => {
        const nextIndex = typeIndices.get(logicalModel.datasource.type) ?? 0;
        typeIndices.set(logicalModel.datasource.type, nextIndex + 1);
        const id = logicalModel.datasource.type + '-' + nextIndex;

        const mappings: GroupMapping[] = [];

        logicalModel.mappings.forEach(mapping => {
            const root = context.objexes.get(mapping.rootObjexKey);
            if (!root) {
                console.error('Root objex not found for mapping', mapping);
                return;
            }

            const properties = [ ...getObjexesFromPath(mapping.accessPath, context).values() ]
                .map(objex => context.objexes.get(objex.key))
                .filter((objex): objex is Objex => !!objex);

            mappings.push({ mapping, root, properties, groupId: id });
        });

        return {
            id,
            logicalModel,
            mappings,
        };
    });
}

function getObjexesFromPath(path: ParentProperty, context: CategoryContext): ComparableSet<Objex, number> {
    const output = new ComparableSet<Objex, number>(objex => objex.key.value);

    path.subpaths.forEach(subpath => {
        findObjexesFromSignature(subpath.signature, context).forEach(objex => output.add(objex));

        // FIXME This needs to be completely reimplemented.
        // if (subpath.name instanceof DynamicName)
        //     findObjexesFromSignature(subpath.name.signature, context).forEach(objex => output.add(objex));

        if (subpath instanceof ComplexProperty)
            getObjexesFromPath(subpath, context).forEach(objex => output.add(objex));
    });

    return output;
}

/** Finds all objexes on the signature path except for the first one. */
function findObjexesFromSignature(signature: Signature, context: CategoryContext): Objex[] {
    const output: Objex[] = [];

    signature.toBases().forEach(rawBase => {
        const objex = findObjexesFromBaseSignature(rawBase, context);
        if (objex)
            output.push(objex);
    });

    return output;
}

function findObjexesFromBaseSignature(rawBase: Signature, context: CategoryContext): Objex | undefined {
    const base = rawBase.isBaseDual ? rawBase.dual() : rawBase;
    const morphism = context.morphisms.get(base);
    if (!morphism)
        return;

    return rawBase.isBaseDual ? morphism.from : morphism.to;
}
