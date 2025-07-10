import { print, type Printable, type Printer } from '@/types/utils/string';
import { type Name, nameFromResponse, type NameResponse, Signature } from '@/types/identifiers';
import { subpathFromResponse, type ChildProperty, type ComplexPropertyResponse } from './ComplexProperty';

// TODO Candice be unified with the ComplexProperty?

export type RootPropertyResponse = ComplexPropertyResponse & { name: NameResponse };

export class RootProperty implements Printable {
    public constructor(
        readonly name: Name,
        readonly subpaths: ChildProperty[],
    ) {}

    readonly isAuxiliary = true;
    readonly signature = Signature.empty();

    static fromResponse(input: RootPropertyResponse): RootProperty {
        const subpaths: ChildProperty[] = [];
        const property = new RootProperty(
            nameFromResponse(input.name),
            subpaths,
        );

        input.subpaths.map(subpath => subpathFromResponse(subpath, property)).forEach(subpath => subpaths.push(subpath));

        return property;
    }

    toServer(): RootPropertyResponse {
        return {
            name: this.name.toServer(),
            signature: this.signature.toServer(),
            subpaths: this.subpaths.map(subpath => subpath.toServer()),
        };
    }

    printTo(printer: Printer): void {
        printer.append(this.name).append(': ');
        if (!this.isAuxiliary)
            printer.append(this.signature).append(' ');

        printer.append('{').down().nextLine();

        for (const subpath of this.subpaths)
            printer.append(subpath).append(',').nextLine();

        printer.remove().up().nextLine()
            .append('}');
    }

    toString(): string {
        return print(this);
    }
}
