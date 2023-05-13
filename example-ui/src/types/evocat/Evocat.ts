import type { Graph } from "@/types/categoryGraph";
import type { SchemaCategory, ObjectDefinition, SchemaObject, MorphismDefinition, SchemaMorphism, SchemaCategoryUpdate } from "@/types/schema";
import type { IdDefinition } from "@/types/identifiers";
import type { LogicalModel } from "../logicalModel";
import type { Result } from "../api/result";
import { VersionContext } from "./Version";

type UpdateFunction = (udpate: SchemaCategoryUpdate) => Promise<Result<SchemaCategory>>;

export type EvocatApi = {
    update: UpdateFunction;
};

export class Evocat {
    private readonly versionContext = VersionContext.createNew();

    private constructor(
        readonly schemaCategory: SchemaCategory,
        readonly logicalModels: LogicalModel[],
        readonly api: EvocatApi,
        private _graph?: Graph,
    ) {}

    static create(schemaCategory: SchemaCategory, logicalModels: LogicalModel[], api: EvocatApi): Evocat {
        const evocat = new Evocat(
            schemaCategory,
            logicalModels,
            api,
        );

        return evocat;
    }

    get graph(): Graph | undefined {
        return this._graph;
    }

    set graph(newGraph: Graph | undefined) {
        this._graph = newGraph;

        if (!newGraph)
            return;

        // TODO

        this.logicalModels.forEach(logicalModel => {
            logicalModel.mappings.forEach(mapping => {
                this.schemaCategory.setDatabaseToObjectsFromMapping(mapping, logicalModel);
            });
        });

        newGraph.getCytoscape().batch(() => {
            this.schemaCategory.objects.forEach(object => newGraph.createNode(object));

            // First we create a dublets of morphisms. Then we create edges from them.
            // TODO there should only be base morphisms
            const sortedBaseMorphisms = this.schemaCategory.morphisms.filter(morphism => morphism.isBase)
                .sort((m1, m2) => m1.sortBaseValue - m2.sortBaseValue);

            sortedBaseMorphisms.forEach(morphism => newGraph.createEdge(morphism));
        });

        // Position the object to the center of the canvas.
        newGraph.fixLayout();
        newGraph.layout();
        newGraph.center();
    }

    private readonly operations = new Map as Map<string, string>;

    private createOperation(name: string) {
        const newVersion = this.versionContext.createNextVersion();
        this.operations.set(newVersion.id, name);
        console.log(`[${newVersion}] : ${name}`);
    }

    compositeOperation<T = void>(name: string, callback: () => T): T {
        this.versionContext.nextLevel();
        const result = callback();
        this.versionContext.prevLevel();
        this.createOperation(name);

        return result;
    }








    addObject(def: ObjectDefinition): SchemaObject {
        const object = this.schemaCategory.createObject(def);
        this._graph?.createNode(object, 'new');

        this.createOperation('addObject');

        return object;
    }

    removeObject(object: SchemaObject): void {
        this.schemaCategory.deleteObject(object);
        this._graph?.deleteNode(object);

        this.createOperation('removeObject');
    }

    addMorphism(def: MorphismDefinition): SchemaMorphism {
        const morphism = this.schemaCategory.createMorphism(def);
        this._graph?.createEdge(morphism, 'new');

        this.createOperation('addMorphism');

        return morphism;
    }

    removeMorphism(morphism: SchemaMorphism): void {
        // TODO The morphism must be removed from all the ids where it's used. Or these ids must be at least revalidated (if only the cardinality changed).
        this.schemaCategory.deleteMorphism(morphism);
        this._graph?.deleteEdge(morphism);

        this.createOperation('removeMorphism');
    }

    addId(object: SchemaObject, def: IdDefinition): void {
        object.addId(def);

        const node = this._graph?.getNode(object);
        node?.updateNoIdsClass();

        this.createOperation('addId');
    }
}
