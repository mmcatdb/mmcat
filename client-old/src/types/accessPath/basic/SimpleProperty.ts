import { IndentedStringBuilder } from '@/utils/string';
import { nameFromServer, Signature, type Name } from '@/types/identifiers';
import type { ParentProperty } from './compositeTypes';
import type { SimplePropertyFromServer } from '../serverTypes';

export class SimpleProperty {
    name: Name;
    parent?: ParentProperty;
    _signature: Signature;

    private constructor(name: Name, signature: Signature, parent?: ParentProperty) {
        this.name = name;
        this._signature = signature;
        this.parent = parent;
    }

    get signature(): Signature {
        return this._signature;
    }

    update(newName: Name, newSignature: Signature) {
        if (!this.name.equals(newName))
            this.name = newName;

        if (!this._signature.equals(newSignature))
            this._signature = newSignature;
    }

    toString(level = 0): string {
        return (level === 0 ? '' : IndentedStringBuilder.getTabIndentationString(level))
            + `${this.name}: ${this._signature}`;
    }

    static fromServer(input: SimplePropertyFromServer, parent: ParentProperty): SimpleProperty {
        return new SimpleProperty(
            nameFromServer(input.name),
            Signature.fromServer(input.signature),
            parent,
        );
    }

    toServer(): SimplePropertyFromServer {
        return {
            name: this.name.toServer(),
            signature: this._signature.toServer(),
        };
    }
}
