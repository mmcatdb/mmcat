import { nameFromServer, Signature, type Name } from '@/types/identifiers';
import { IndentedStringBuilder } from '@/utils/string';
import type { ComplexPropertyFromServer } from '../serverTypes';
import { subpathFromFromServer, type ChildProperty, type ParentProperty } from './compositeTypes';

export class ComplexProperty {
    private constructor(
        readonly name: Name,
        readonly _signature: Signature,
        readonly parent: ParentProperty | undefined,
        readonly _subpaths: ChildProperty[],
    ) {}

    get isAuxiliary(): boolean {
        return this._signature.isEmpty;
    }

    get signature(): Signature {
        return this._signature;
    }

    get subpaths(): ChildProperty[] {
        return this._subpaths;
    }

    toString(level = 0): string {
        const builder = new IndentedStringBuilder(level);

        builder.appendIntendedLine(this.name + ': ');
        if (!this.isAuxiliary)
            builder.append(this.signature + ' ');
        builder.append('{\n');

        const subpathsAsString = this.subpaths.map(path => path.toString(level + 1)).join(',\n');
        builder.append(subpathsAsString);

        builder.appendIntendedLine('}');

        return builder.toString();
    }

    static fromServer(input: ComplexPropertyFromServer, parent: ParentProperty): ComplexProperty {
        const subpaths: ChildProperty[] = [];
        const property = new ComplexProperty(
            nameFromServer(input.name),
            Signature.fromServer(input.signature),
            parent,
            subpaths,
        );

        input.subpaths.map(subpath => subpathFromFromServer(subpath, property)).forEach(subpath => subpaths.push(subpath));

        return property;
    }

    toServer(): ComplexPropertyFromServer {
        return {
            name: this.name.toServer(),
            signature: this._signature.toServer(),
            subpaths: this._subpaths.map(subpath => subpath.toServer()),
        };
    }
}
