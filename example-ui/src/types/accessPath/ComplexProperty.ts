import { DynamicName, nameFromJSON, Signature, StaticName, type Name, type NameJSON, type SignatureJSON } from "../identifiers";
import { IntendedStringBuilder } from "@/utils/string";
import { subpathFromJSON, type AccessPathJSON } from "./AccessPath";
import type { ChildProperty, ParentProperty } from "./compositeTypes";
import type { NodeSchemaData } from "../categoryGraph";

export type ComplexPropertyJSON = { _class: 'ComplexProperty', name: NameJSON, signature: SignatureJSON, subpaths: AccessPathJSON[] };

export class ComplexProperty {
    public name: Name;
    private _signature: Signature;
    public parent?: ParentProperty;
    private _node?: NodeSchemaData;
    private _subpaths: ChildProperty[];
    //private _subpaths = new ComparableMap<Signature, string, AccessPath>(signature => signature.toString());

    public constructor(name: Name, signature: Signature, parent?: ParentProperty, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._signature = signature;
        this.parent = parent;
        this._subpaths = [ ...subpaths ];
        //subpaths.forEach(subpath => this._subpaths.set(subpath.signature, subpath));
    }

    public static copy(property: ComplexProperty): ComplexProperty {
        const name = property.name instanceof DynamicName ? DynamicName.copy(property.name) : StaticName.copy(property.name);
        return new ComplexProperty(name, Signature.copy(property.signature), property.parent, property.subpaths);
    }

    public update(newName: Name, newSignature: Signature): void {
        if (!this.name.equals(newName))
            this.name = newName;

        if (!this._signature.equals(newSignature)) {
            this._signature = newSignature;
            //this._subpaths.clear();
            this._subpaths = [];
        }
    }

    public updateOrAddSubpath(newSubpath: ChildProperty, oldSubpath?: ChildProperty): void {
        newSubpath.parent = this;
        const index = oldSubpath ? this._subpaths.findIndex(subpath => subpath.signature.equals(oldSubpath.signature)) : -1;
        if (index === -1)
            this._subpaths.push(newSubpath);
        else
            this._subpaths[index] = newSubpath;

        if (this._node)
            newSubpath.node = this._node.getNeighbour(newSubpath.signature);
    }

    public get isAuxiliary(): boolean {
        return this.signature.isNull;
    }

    public get signature(): Signature {
        return this._signature;
    }

    public get node(): NodeSchemaData | undefined {
        return this._node;
    }

    public set node(value: NodeSchemaData | undefined) {
        this._node = value;
    }

    public get subpaths(): ChildProperty[] {
        return this._subpaths;
    }

    public toString(level = 0): string {
        const builder = new IntendedStringBuilder(level);

        builder.appendIntendedLine(this.name + ': ');
        if (!this.isAuxiliary)
            builder.append(this.signature + ' ');
        builder.append('{\n');

        const subpathsAsString = this.subpaths.map(path => path.toString(level + 1)).join(',\n');
        builder.append(subpathsAsString);

        builder.appendIntendedLine('}');

        return builder.toString();
    }

    public static fromJSON(jsonObject: ComplexPropertyJSON, parent: ParentProperty): ComplexProperty {
        const property = new ComplexProperty(
            nameFromJSON(jsonObject.name),
            Signature.fromJSON(jsonObject.signature),
            parent
        );

        property._subpaths = jsonObject.subpaths.map(subpath => subpathFromJSON(subpath, property));

        return property;
    }
}
