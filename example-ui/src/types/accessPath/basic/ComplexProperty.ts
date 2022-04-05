import { DynamicName, nameFromJSON, Signature, StaticName, type Name, type NameJSON, type SignatureJSON } from "@/types/identifiers";
import { IntendedStringBuilder } from "@/utils/string";
import { subpathFromJSON, type AccessPathJSON } from "./AccessPath";
import type { ChildProperty, ParentProperty } from "./compositeTypes";

export type ComplexPropertyJSON = { _class: 'ComplexProperty', name: NameJSON, signature: SignatureJSON, subpaths: AccessPathJSON[] };

export class ComplexProperty {
    public name: Name;
    private _signature: Signature;
    public parent?: ParentProperty;
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

    public get isAuxiliary(): boolean {
        return this.signature.isNull;
    }

    public get signature(): Signature {
        return this._signature;
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

    public toJSON(): ComplexPropertyJSON {
        return {
            _class: 'ComplexProperty',
            name: this.name.toJSON(),
            signature: this._signature.toJSON(),
            subpaths: this._subpaths.map(subpath => subpath.toJSON())
        };
    }
}
