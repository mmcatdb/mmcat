import { IntendedStringBuilder } from "@/utils/string";
import { Signature, StaticName, type StaticNameJSON } from "@/types/identifiers";
import type { ComplexPropertyJSON } from "./ComplexProperty";
import { subpathFromJSON, type ChildProperty } from "./compositeTypes";

export type RootPropertyJSON = ComplexPropertyJSON & { name: StaticNameJSON };

export class RootProperty {
    public name: StaticName;
    private _subpaths: ChildProperty[];
    private _signature = Signature.null;

    public constructor(name: StaticName, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._subpaths = [ ...subpaths ];
    }

    public get subpaths(): ChildProperty[] {
        return this._subpaths;
    }

    public static fromJSON(jsonObject: RootPropertyJSON): RootProperty {
        const property = new RootProperty(StaticName.fromJSON(jsonObject.name));

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
