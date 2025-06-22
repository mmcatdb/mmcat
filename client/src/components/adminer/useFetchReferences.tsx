import { useState, useEffect } from 'react';
import { api } from '@/api';
import { Signature } from '@/types/identifiers/Signature';
import { nameFromServer, StringName, type NameFromServer } from '@/types/identifiers/Name';
import type { SignatureIdFromServer } from '@/types/identifiers/SignatureId';
import type { AdminerReferenceKind, AdminerReferences } from '@/types/adminer/AdminerReferences';
import type { ChildPropertyFromServer, ComplexPropertyFromServer } from '@/types/mapping/ComplexProperty';
import type { RootPropertyFromServer } from '@/types/mapping/RootProperty';
import type { SimplePropertyFromServer } from '@/types/mapping/SimpleProperty';
import type { SchemaCategoryFromServer } from '@/types/schema';
import type { MappingFromServer, MappingInit } from '@/types/mapping';
import type { Id } from '@/types/id';

const FROM_PREFIX = '_from.';
const TO_PREFIX = '_to.';

/**
 * Fetches all references (foreign keys and references from schema category)
 */
export function useFetchReferences(datasourceId?: Id, kindName?: string) {
    const [ loading, setLoading ] = useState(true);
    const [ references, setReferences ] = useState<AdminerReferences>([]);

    useEffect(() => {
        void (async () => {
            setLoading(true);

            try {
                if (datasourceId && kindName) {
                    const foreignKeys = await getForeignKeys(datasourceId, kindName);
                    const schemaCategoryReferences = await getSchemaCategoryReferences(datasourceId, kindName);

                    setReferences(foreignKeys.concat(schemaCategoryReferences));
                }
            }
            catch (e) {
                console.log(e);
            }

            setLoading(false);
        })();
    }, [ datasourceId, kindName ]);

    return { references: references, referencesLoading: loading };
}

async function getForeignKeys(datasourceId: Id, kindName: string): Promise<AdminerReferences> {
    const foreignKeysResponse = await api.adminer.getReferences({ datasourceId: datasourceId }, { kindName: kindName });

    if (!foreignKeysResponse.status)
        throw new Error(`Failed to fetch foreign keys`);

    const foreignKeys: AdminerReferences = [];
    for (const fk of foreignKeysResponse.data) {
        foreignKeys.push(fk);
        foreignKeys.push({ from: fk.to, to: fk.from });
    }

    return foreignKeys;
}

async function getSchemaCategoryReferences(datasourceId: Id, kindName: string): Promise<AdminerReferences> {
    const allMappings = await getAllMappings();
    const mappings: MappingInit[] = extractAllKindMappings(allMappings);

    // All mappings for given kind
    const kindMappings = mappings.filter(mapping =>
        mapping.kindName === kindName &&
        mapping.datasourceId === datasourceId);

    if (kindMappings.length === 0)
        return [];

    const primaryKeys = getAllPrimaryKeys(mappings);

    const references: AdminerReferences = [];
    for (const kindMapping of kindMappings) {
        // Schema category that includes the given mapping of given kind
        const schemaCategory = await getSchemaCategory(kindMapping.categoryId);

        // All mappings in the given schema category
        const schemaCategoryMappings = mappings.filter(mapping =>
            mapping.categoryId === schemaCategory.id);

        const mappingPathProperties = getPropertiesFromAccessPath(kindMapping.accessPath, new Set<SimplePropertyFromServer>());

        for (const mapping of schemaCategoryMappings)
            addMappingReferences(references, primaryKeys, mapping, kindMapping, mappingPathProperties);
    }

    return references;
}

async function getAllMappings(): Promise<MappingFromServer[]> {
    const mappingResponse = await api.mappings.getAllMappings({});

    if (!mappingResponse.status)
        throw new Error(`Failed to fetch mappings`);

    return mappingResponse.data;
}

// FIXME This shouldn't be done on the "from server" types.
function extractAllKindMappings(mappings: MappingFromServer[]): MappingInit[] {
    return mappings.flatMap((mapping: MappingFromServer) => {
        let fromSubpath: ComplexPropertyFromServer | undefined = undefined;
        let toSubpath: ComplexPropertyFromServer | undefined = undefined;
        const allSubpaths: ChildPropertyFromServer[] = [];

        for (let subpath of mapping.accessPath.subpaths) {
            const name = nameFromServer(subpath.name);
            if (name instanceof StringName) {
                const nameValue = name.value;
                // FIXME use typed name
                if (nameValue.startsWith(FROM_PREFIX)) {
                    fromSubpath = subpath as ComplexPropertyFromServer;
                    subpath = prefixSubpathNames(subpath, 'from', 0);
                }
                else if (nameValue.startsWith(TO_PREFIX)) {
                    toSubpath = subpath as ComplexPropertyFromServer;
                    subpath = prefixSubpathNames(subpath, 'to', 0);
                }

                allSubpaths.push(subpath);
            }
            else {
                allSubpaths.push(subpath);
            }
        }

        const newMappings: MappingInit[] = [];

        if (fromSubpath) {
            const kindName = (fromSubpath.name as { value: string }).value;

            // Mapping for kind that is used as a start node of a relationship kind
            newMappings.push({
                categoryId: mapping.categoryId,
                datasourceId: mapping.datasourceId,
                rootObjexKey: mapping.rootObjexKey,
                primaryKey: mapping.primaryKey,
                kindName: kindName.startsWith(FROM_PREFIX) ? kindName.substring(FROM_PREFIX.length) : kindName,
                accessPath: {
                    name: { type: 'ANONYMOUS', value: '' },
                    signature: fromSubpath.signature,
                    subpaths: fromSubpath.subpaths || [],
                },
            });
        }

        if (toSubpath) {
            const kindName = (toSubpath.name as { value: string }).value;

            // Mapping for kind that is used as an end node of a relationship kind
            newMappings.push({
                categoryId: mapping.categoryId,
                datasourceId: mapping.datasourceId,
                rootObjexKey: mapping.rootObjexKey,
                primaryKey: mapping.primaryKey,
                kindName: kindName.startsWith(TO_PREFIX) ? kindName.substring(TO_PREFIX.length) : kindName,
                accessPath: {
                    name: { type: 'ANONYMOUS', value: '' },
                    signature: toSubpath.signature,
                    subpaths: toSubpath.subpaths || [],
                },
            });
        }

        if (fromSubpath ?? toSubpath) {
            // Maapping with prefixed names
            newMappings.push({
                categoryId: mapping.categoryId,
                datasourceId: mapping.datasourceId,
                rootObjexKey: mapping.rootObjexKey,
                primaryKey: mapping.primaryKey,
                kindName: mapping.kindName,
                accessPath: {
                    name: mapping.accessPath.name,
                    signature: mapping.accessPath.signature,
                    subpaths: allSubpaths,
                },
            });
        }
        else {
            newMappings.push(mapping);
        }

        return newMappings;
    });
}

/**
* Adds given prefix to all names in access path of a relationship kind excluding names of node kinds.
*/
function prefixSubpathNames(subpath: ChildPropertyFromServer, prefix: string, level: number): ChildPropertyFromServer {
    let prefixedName: NameFromServer = subpath.name;
    if ('value' in subpath.name && level > 0)
        prefixedName = { value: `${prefix}.${subpath.name.value}` };

    if ('subpaths' in subpath) {
        return {
            ...subpath,
            name: prefixedName,
            subpaths: subpath.subpaths.map(child => prefixSubpathNames(child, prefix, level + 1)),
        };
    }
    else {
        return {
            ...subpath,
            name: prefixedName,
        };
    }
}

function getAllPrimaryKeys(kindMappings: MappingInit[]): SignatureIdFromServer {
    const primaryKeys: SignatureIdFromServer = [];

    for (const mapping of kindMappings) {
        for (const key of mapping.primaryKey) {
            const signatureBase = getLastBase(key);
            if (!primaryKeys.includes(signatureBase))
                primaryKeys.push(signatureBase);
        }
    }

    return primaryKeys;
}

async function getSchemaCategory(categoryId: string): Promise<SchemaCategoryFromServer> {
    const schemaCategoryResponse = await api.schemas.getCategory({ id: categoryId });

    if (!schemaCategoryResponse.status)
        throw new Error(`Failed to fetch schema categories`);

    return schemaCategoryResponse.data;
}

/**
* Returns a set of objects with name and signature of each objex in the given access path.
*/
function getPropertiesFromAccessPath(
    accessPath: RootPropertyFromServer | ChildPropertyFromServer,
    properties: Set<SimplePropertyFromServer>,
): Set<SimplePropertyFromServer>{
    if ('signature' in accessPath && accessPath.signature !== 'EMPTY')
        properties.add({ name: accessPath.name, signature: getLastBase(accessPath.signature) });

    if ('subpaths' in accessPath && Array.isArray(accessPath.subpaths)) {
        for (const subpath of accessPath.subpaths)
            getPropertiesFromAccessPath(subpath, properties);
    }

    return properties;
}

/**
* Returns the last base of a given signature.
*/
function getLastBase(signatureFromServer: string): string {
    const signature: Signature = Signature.fromServer(signatureFromServer);
    const lastBase = signature.tryGetLastBase();
    return lastBase!.last.toString();
}

/**
* Extends list of references.
* Adds references between mapping of given kind ('givenKindMapping') and another mapping from the same schema category ('anotherKindMapping').
*/
function addMappingReferences(
    references: AdminerReferences,
    primaryKeys: SignatureIdFromServer,
    anotherKindMapping: MappingInit,
    givenKindMapping: MappingInit,
    givenKindProperties: Set<SimplePropertyFromServer>,
): AdminerReferences {
    const anotherKindProperties = getPropertiesFromAccessPath(anotherKindMapping.accessPath, new Set<SimplePropertyFromServer>());
    const anotherKindPropertiesArray = Array.from(anotherKindProperties);
    const givenKindPropertiesArray = Array.from(givenKindProperties);

    addReferences(references, primaryKeys, givenKindMapping, givenKindPropertiesArray, anotherKindMapping, anotherKindPropertiesArray);

    return references;
}

function addReferences(
    references: AdminerReferences,
    primaryKeys: SignatureIdFromServer,
    fromKindMapping: MappingInit,
    fromKindProperties: SimplePropertyFromServer[],
    toKindMapping: MappingInit,
    toKindProperties: SimplePropertyFromServer[],
) {
    for (const keySignature of primaryKeys) {
        // Name of properties that are primary keys in the given kind
        const fromKindKeyProperties = getKeyProperties(fromKindProperties, keySignature);

        for (const fromKindNameProp of fromKindKeyProperties) {
            const fromKindKeyName = getPropertyName(fromKindNameProp);

            if (fromKindKeyName.length == 0)
                continue;

            // All properties from the another kind with signature equal to the last base of the signature of the given key of given kind
            const toKindKeyProperties = getKeyProperties(toKindProperties, keySignature);

            for (const toKindKeyProp of toKindKeyProperties) {
                const toKindKeyName = getPropertyName(toKindKeyProp);

                if (toKindKeyName.length == 0)
                    continue;

                const fromKindReference = getKindReference(fromKindMapping, fromKindKeyName);
                const toKindReference = getKindReference(toKindMapping, toKindKeyName);

                addReference(references, fromKindReference, toKindReference);
                addReference(references, toKindReference, fromKindReference);
            }
        }
    }
}

function getKeyProperties(properties: SimplePropertyFromServer[], keySignature: string): SimplePropertyFromServer[] {
    return properties
        .filter(property => property.signature === keySignature && 'value' in property.name);
}

function getPropertyName(property: SimplePropertyFromServer): string {
    return (property.name as { value: string }).value;
}

function getKindReference(mapping: MappingInit, propertyName: string): AdminerReferenceKind {
    return {
        datasourceId: mapping.datasourceId,
        kindName: mapping.kindName,
        property: propertyName,
    };
}

function addReference(references: AdminerReferences, from: AdminerReferenceKind, to: AdminerReferenceKind) {
    if (from.datasourceId !== to.datasourceId || from.kindName !== to.kindName || from.property !== to.property) {
        references.push({
            from: from,
            to: to,
        });
    }
}
