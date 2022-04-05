import { IntendedStringBuilder } from "@/utils/string";
import { nameFromJSON, Signature, type Name } from "@/types/identifiers";
import type { ComplexPropertyJSON } from "./ComplexProperty";
import { subpathFromJSON, type ChildProperty } from "./compositeTypes";

export class RootProperty {
    public name: Name; // TODO should be static name
    private _subpaths: ChildProperty[];
    private _signature = Signature.null;

    public constructor(name: Name, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._subpaths = [ ...subpaths ];
    }

    public get subpaths(): ChildProperty[] {
        return this._subpaths;
    }

    public static fromJSON(jsonObject: ComplexPropertyJSON): RootProperty {
        const property = new RootProperty(nameFromJSON(jsonObject.name));

        property._subpaths = jsonObject.subpaths.map(subpath => subpathFromJSON(subpath, property));

        return property;
    }

    public toString(level = 0): string {
        const builder = new IntendedStringBuilder(level);

        builder.appendIntendedLine(this.name + ': ');
        builder.append('{\n');

        const subpathsAsString = this.subpaths.map(path => path.toString(level + 1)).join(',\n');
        builder.append(subpathsAsString);

        builder.appendIntendedLine('}');

        return builder.toString();
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
