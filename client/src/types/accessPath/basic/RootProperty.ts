import { IntendedStringBuilder } from '@/utils/string';
import { Signature, StaticName } from '@/types/identifiers';
import { subpathFromFromServer, type ChildProperty } from './compositeTypes';
import type { RootPropertyFromServer } from '../serverTypes';

export class RootProperty {
    name: StaticName;
    _subpaths: ChildProperty[];
    _signature = Signature.empty;

    constructor(name: StaticName, subpaths: ChildProperty[] = []) {
        this.name = name;
        this._subpaths = [ ...subpaths ];
    }

    get isAuxiliary(): boolean {
        return true;
    }

    get signature(): Signature {
        return this._signature;
    }

    get subpaths(): ChildProperty[] {
        return this._subpaths;
    }

    static fromServer(input: RootPropertyFromServer): RootProperty {
        const property = new RootProperty(StaticName.fromServer(input.name));

        property._subpaths = input.subpaths.map(subpath => subpathFromFromServer(subpath, property));

        return property;
    }

    toString(level = 0): string {
        const builder = new IntendedStringBuilder(level);

        builder.appendIntendedLine(this.name + ': ');
        builder.append('{\n');

        const subpathsAsString = this.subpaths.map(path => path.toString(level + 1)).join(',\n');
        builder.append(subpathsAsString);

        builder.appendIntendedLine('}');

        return builder.toString();
    }

    toServer(): RootPropertyFromServer {
        return {
            name: this.name.toServer(),
            signature: this._signature.toServer(),
            isAuxiliary: true,
            subpaths: this._subpaths.map(subpath => subpath.toServer()),
        };
    }
}
