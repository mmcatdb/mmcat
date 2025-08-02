import { Signature, type NamePath, type Name } from '../identifiers';

/** Editable, React-friendly version of access path. */
export type AccessPath = {
    name: Name;
    signature: Signature;
    subpaths: AccessPath[];
    isRoot: boolean;
    // No reference to parent because that would require to always change the whole structure when replacing any single object.
};

/** Creates a shallow copy of the property. */
function copyAccessPath(property: AccessPath): AccessPath {
    return {
        ...property,
        subpaths: [ ...property.subpaths ],
    };
}

export function traverseAccessPath(property: AccessPath, path: NamePath): AccessPath {
    let current = property;

    for (const name of path.names) {
        const child = current.subpaths.find(s => s.name.equals(name));
        if (!child)
            throw subpathNotFoundError(current, name);

        current = child;
    }

    return current;
}

export function collectAccessPathSignature(property: AccessPath, path: NamePath): Signature {
    const properties = traverseAndCollect(property, path);
    return Signature.concatenate(...properties.map(p => p.signature));
}

function traverseAndCollect(property: AccessPath, path: NamePath): AccessPath[] {
    const output: AccessPath[] = [];
    let current = property;

    for (const name of path.names) {
        const child = current.subpaths.find(s => s.name.equals(name));
        if (!child)
            throw subpathNotFoundError(current, name);

        current = child;
        output.push(current);
    }

    return output;
}

/**
 * Replaces the last property on the path with the given edit. All properties on the path are also refreshed.
 * @param path Path from the root to the property to edit (inclusive). Can't be empty.
 * @param edit Replaces the property (or inserts a new one, if the old doesn't exist). If undefined, the property is removed.
 * @returns The refreshed property on the start of the path (a shallow copy of the `property`).
 */
export function updateAccessPath(property: AccessPath, path: NamePath, edit: AccessPath | undefined): AccessPath {
    const output = copyAccessPath(property);

    // Travel to the parent of the updated path, updating all properties along the way.
    let parent = output;

    for (let i = 0; i < path.names.length - 1; i++) {
        const name = path.names[i];
        const indexInParent = parent.subpaths.findIndex(s => s.name.equals(name));
        if (indexInParent === -1)
            throw subpathNotFoundError(parent, name);

        const subpath = copyAccessPath(parent.subpaths[indexInParent]);
        parent.subpaths[indexInParent] = subpath;

        parent = subpath;
    }

    if (path.names.length === 0)
        throw new Error('Name path can\'t be empty.');

    const lastName = path.names[path.names.length - 1];

    if (edit === undefined) {
        // Remove the last property.
        parent.subpaths = parent.subpaths.filter(s => !s.name.equals(lastName));
        return output;
    }

    const indexInParent = parent.subpaths.findIndex(s => s.name.equals(lastName));
    if (indexInParent === -1) {
        // Insert the new property.
        parent.subpaths.push(edit);
        return output;
    }

    // Replace the last property.
    parent.subpaths[indexInParent] = edit;
    return output;
}

function subpathNotFoundError(parent: AccessPath, name: Name): Error {
    return new Error(
        `Name "${name.toString()}" not found in access path "${parent.name.toString()}".\n` +
        `Subpaths:\n${parent.subpaths.map(s => s.name.toString()).join('\n')}`,
    );
}
