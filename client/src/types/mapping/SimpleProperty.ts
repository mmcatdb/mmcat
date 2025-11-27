import { print, type Printable, type Printer } from '@/types/utils/string';
import { type NameResponse, nameFromResponse, Signature, type SignatureResponse, type Name, TypedName } from '@/types/identifiers';
import { PRINTER_CONTEXT_SHORT_FORM, type ParentProperty } from './ComplexProperty';
import { type AccessPath } from './AccessPath';

export type SimplePropertyResponse = {
    name: NameResponse;
    signature: SignatureResponse;
};

export class SimpleProperty implements Printable {
    constructor(
        readonly name: Name,
        readonly signature: Signature,
        readonly parent: ParentProperty,
    ) {}

    static fromResponse(input: SimplePropertyResponse, parent: ParentProperty): SimpleProperty {
        return new SimpleProperty(
            nameFromResponse(input.name),
            Signature.fromResponse(input.signature),
            parent,
        );
    }

    toServer(): SimplePropertyResponse {
        return {
            name: this.name.toServer(),
            signature: this.signature.toServer(),
        };
    }

    printTo(printer: Printer): void {
        const showName = !printer.context(PRINTER_CONTEXT_SHORT_FORM) || !(this.name instanceof TypedName) || this.name.type !== TypedName.VALUE;
        if (showName)
            printer.append(this.name).append(': ');

        printer.append(this.signature);
    }

    toString(): string {
        return print(this);
    }

    toEditable(): AccessPath {
        return {
            name: this.name,
            signature: this.signature,
            subpaths: [],
            isRoot: false,
        };
    }
}
