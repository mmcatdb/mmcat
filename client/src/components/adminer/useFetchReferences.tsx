import { useState, useEffect } from 'react';
import { api } from '@/api';
import { Signature } from '@/types/identifiers/Signature';
import { nameFromResponse, StringName, type NameResponse } from '@/types/identifiers/Name';
import type { SignatureIdResponse } from '@/types/identifiers/SignatureId';
import type { AdminerReferenceKind, AdminerReferences } from '@/types/adminer/AdminerReferences';
import type { ChildPropertyResponse, ComplexPropertyResponse, RootPropertyResponse, SimplePropertyResponse } from '@/types/mapping';
import type { SchemaCategoryResponse } from '@/types/schema';
import type { MappingResponse, MappingInit } from '@/types/mapping';
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

        const mappingPathProperties = getPropertiesFromAccessPath(kindMapping.accessPath, new Set<SimplePropertyResponse>());

        for (const mapping of schemaCategoryMappings)
            addMappingReferences(references, primaryKeys, mapping, kindMapping, mappingPathProperties);
    }

    return references;
}

async function getAllMappings(): Promise<MappingResponse[]> {
    const mappingResponse = await api.mappings.getAllMappings({});

    if (!mappingResponse.status)
        throw new Error(`Failed to fetch mappings`);

    return mappingResponse.data;
}

// FIXME This shouldn't be done on the "from server" types.
function extractAllKindMappings(mappings: MappingResponse[]): MappingInit[] {
    return mappings.flatMap((mapping: MappingResponse) => {
        let fromSubpath: ComplexPropertyResponse | undefined = undefined;
        let toSubpath: ComplexPropertyResponse | undefined = undefined;
        const allSubpaths: ChildPropertyResponse[] = [];

        for (let subpath of mapping.accessPath.subpaths) {
            const name = nameFromResponse(subpath.name);
            if (name instanceof StringName) {
                const nameValue = name.value;
                // FIXME use typed name
                if (nameValue.startsWith(FROM_PREFIX)) {
                    fromSubpath = subpath as ComplexPropertyResponse;
                    subpath = prefixSubpathNames(subpath, 'from', 0);
                }
                else if (nameValue.startsWith(TO_PREFIX)) {
                    toSubpath = subpath as ComplexPropertyResponse;
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
function prefixSubpathNames(subpath: ChildPropertyResponse, prefix: string, level: number): ChildPropertyResponse {
    let prefixedName: NameResponse = subpath.name;
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

function getAllPrimaryKeys(kindMappings: MappingInit[]): SignatureIdResponse {
    const primaryKeys: SignatureIdResponse = [];

    for (const mapping of kindMappings) {
        for (const key of mapping.primaryKey) {
            const signatureBase = getLastBase(key);
            if (!primaryKeys.includes(signatureBase))
                primaryKeys.push(signatureBase);
        }
    }

    return primaryKeys;
}

async function getSchemaCategory(categoryId: Id): Promise<SchemaCategoryResponse> {
    const schemaCategoryResponse = await api.schemas.getCategory({ id: categoryId });

    if (!schemaCategoryResponse.status)
        throw new Error(`Failed to fetch schema categories`);

    return schemaCategoryResponse.data;
}

/**
* Returns a set of objects with name and signature of each objex in the given access path.
*/
function getPropertiesFromAccessPath(
    accessPath: RootPropertyResponse | ChildPropertyResponse,
    properties: Set<SimplePropertyResponse>,
): Set<SimplePropertyResponse>{
    // FIXME Use from server to avoid the reference to the 'EMPTY' literal
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
function getLastBase(signatureResponse: string): string {
    const signature: Signature = Signature.fromResponse(signatureResponse);
    const lastBase = signature.tryGetLastBase();
    return lastBase!.last.toString();
}

/**
* Extends list of references.
* Adds references between mapping of given kind ('givenKindMapping') and another mapping from the same schema category ('anotherKindMapping').
*/
function addMappingReferences(
    references: AdminerReferences,
    primaryKeys: SignatureIdResponse,
    anotherKindMapping: MappingInit,
    givenKindMapping: MappingInit,
    givenKindProperties: Set<SimplePropertyResponse>,
): AdminerReferences {
    const anotherKindProperties = getPropertiesFromAccessPath(anotherKindMapping.accessPath, new Set<SimplePropertyResponse>());
    const anotherKindPropertiesArray = [ ...anotherKindProperties ];
    const givenKindPropertiesArray = [ ...givenKindProperties ];

    addReferences(references, primaryKeys, givenKindMapping, givenKindPropertiesArray, anotherKindMapping, anotherKindPropertiesArray);

    return references;
}

function addReferences(
    references: AdminerReferences,
    primaryKeys: SignatureIdResponse,
    fromKindMapping: MappingInit,
    fromKindProperties: SimplePropertyResponse[],
    toKindMapping: MappingInit,
    toKindProperties: SimplePropertyResponse[],
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

function getKeyProperties(properties: SimplePropertyResponse[], keySignature: string): SimplePropertyResponse[] {
    return properties
        .filter(property => property.signature === keySignature && 'value' in property.name);
}

function getPropertyName(property: SimplePropertyResponse): string {
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
