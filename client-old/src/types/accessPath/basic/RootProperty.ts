import { IndentedStringBuilder } from '@/utils/string';
import { type Name, nameFromServer, Signature } from '@/types/identifiers';
import { subpathFromFromServer, type ChildProperty } from './compositeTypes';
import type { ComplexPropertyFromServer } from '../serverTypes';

export class RootProperty {
    name: Name;
    _subpaths: ChildProperty[];
    _signature = Signature.empty;

    constructor(name: Name, subpaths: ChildProperty[] = []) {
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

    static fromServer(input: ComplexPropertyFromServer): RootProperty {
        const property = new RootProperty(nameFromServer(input.name));

        property._subpaths = input.subpaths.map(subpath => subpathFromFromServer(subpath, property));

        return property;
    }

    toString(level = 0): string {
        const builder = new IndentedStringBuilder(level);

        builder.appendIndentedLine('{\n');

        const subpathsAsString = this.subpaths.map(path => path.toString(level + 1)).join(',\n');
        builder.append(subpathsAsString);

        builder.appendIndentedLine('}');

        return builder.toString();
    }

    toServer(): ComplexPropertyFromServer {
        return {
            name: this.name.toServer(),
            signature: this._signature.toServer(),
            subpaths: this._subpaths.map(subpath => subpath.toServer()),
        };
    }
}
