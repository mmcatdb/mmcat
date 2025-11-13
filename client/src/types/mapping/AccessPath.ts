import { Signature, type NamePath, type Name, DynamicName, IndexName, TypedName, StringName } from '../identifiers';

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
 * @returns The refreshed property on the start of the path (a shallow copy of the <code>property</code>).
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

export enum AccessPathType {
    default = 'default',
    map = 'map',
    array = 'array',
    special = 'special',
}

export function getAccessPathType(property: AccessPath): AccessPathType {
    if (property.name instanceof DynamicName)
        return AccessPathType.map;
    if (findIndexSubpaths(property).length > 0)
        return AccessPathType.array;
    if (property.name instanceof TypedName)
        return AccessPathType.special;
    return AccessPathType.default;
}

export function findTypedSubpath(property: AccessPath, type: string): AccessPath | undefined {
    return property.subpaths.find(s => s.name instanceof TypedName && s.name.type === type);
}

export function findIndexSubpaths(property: AccessPath): AccessPath[] {
    return property.subpaths.filter(s => s.name instanceof IndexName);
}

export function transformAccessPathType(nextType: AccessPathType, prev: AccessPath, original: AccessPath): AccessPath {
    const prevType = getAccessPathType(prev);
    if (nextType === prevType)
        return prev;

    // String names can be reused without any issues. We want to prioritize the previous name first.
    const nextStringName = (prev.name instanceof StringName ? prev.name : undefined) ??
        (original.name instanceof StringName ? original.name : undefined) ??
        new StringName('');

    const originalType = getAccessPathType(original);
    if (nextType === originalType && prev.signature.equals(original.signature)) {
        // If the signature didn't change, we can just reuse the original property.
        // Except for the string name, for which we always want the latest valid version.
        return original.name instanceof StringName ? { ...original, name: nextStringName } : original;
    }

    const isNextComposed = nextType === AccessPathType.map || nextType === AccessPathType.array;
    const isPrevComposed = prevType === AccessPathType.map || prevType === AccessPathType.array;

    const prevValuePath = findTypedSubpath(prev, TypedName.VALUE);

    if (!isNextComposed) {
        const name = nextType === AccessPathType.special ? new TypedName(TypedName.VALUE) : nextStringName;
        if (!isPrevComposed)
            // Default -> special (or vice versa).
            return { ...prev, name };

        // Composed -> plain - we just decompose the previous property.
        return {
            ...prev,
            name,
            signature: prev.signature.concatenate(prevValuePath!.signature),
            subpaths: prevValuePath!.subpaths,
        };
    }

    const name = nextType === AccessPathType.map ? new DynamicName() : nextStringName;
    const nextKeyOrIndexPath = {
        name: nextType === AccessPathType.map ? new TypedName(TypedName.KEY) : new IndexName(0),
        signature: Signature.empty(),
        subpaths: [],
        isRoot: false,
    } satisfies AccessPath;

    if (isPrevComposed) {
        // Map -> array (or vice versa).
        return {
            ...prev,
            name,
            subpaths: [ nextKeyOrIndexPath, prevValuePath! ],
        };
    }

    // Plain -> composed - we just compose the next property.
    const nextValuePath = {
        name: new TypedName(TypedName.VALUE),
        signature: Signature.empty(),
        subpaths: prev.subpaths,
        isRoot: false,
    } satisfies AccessPath;

    return {
        ...prev,
        name,
        subpaths: [ nextKeyOrIndexPath, nextValuePath ],
    };
}
