import { useState, useEffect } from 'react';
import { api } from '@/api';
import { Signature } from '@/types/identifiers/Signature';
import type { StaticNameFromServer } from '@/types/identifiers/Name';
import type { SignatureIdFromServer } from '@/types/identifiers/SignatureId';
import type { AdminerFilterQueryState } from '@/types/adminer/ReducerTypes';
import type { AdminerReferences } from '@/types/adminer/AdminerReferences';
import type { ChildPropertyFromServer, ComplexPropertyFromServer, RootPropertyFromServer, SimplePropertyFromServer } from '@/types/accessPath/serverTypes';
import type { SchemaCategoryFromServer } from '@/types/schema';
import type { MappingFromServer, MappingInit } from '@/types/mapping';
import type { Id } from '@/types/id';

function getLastBase(signature: string): string {
    const signatureFromServer: Signature = Signature.fromServer(signature);
    const lastBase = signatureFromServer.getLastBase();
    return lastBase!.last.toString();
}

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

function addReferences(
    references: AdminerReferences,
    primaryKeys: SignatureIdFromServer,
    targetMapping: MappingInit,
    sourceMapping: MappingInit,
    targetProperties: SimplePropertyFromServer[],
    sourceProperties: SimplePropertyFromServer[],
    primaryKeyInTarget: boolean,
) {
    for (const key of primaryKeys) {
        const keyLastBase = getLastBase(key);

        const keyPropertyValue = targetProperties
            .filter(property => property.signature === keyLastBase && 'value' in property.name)
            .map(property => (property.name as StaticNameFromServer).value)[0] || '';

        sourceProperties
            .filter(mappingProp => mappingProp.signature === keyLastBase && 'value' in mappingProp.name)
            .forEach(mappingProp => {
                const mappingPropertyValue = (mappingProp.name as StaticNameFromServer).value;
                references.push({
                    referencedDatasourceId: targetMapping.datasourceId,
                    referencedKindName: targetMapping.kindName,
                    referencedProperty: primaryKeyInTarget ? keyPropertyValue : mappingPropertyValue,
                    referencingDatasourceId: sourceMapping.datasourceId,
                    referencingKindName: sourceMapping.kindName,
                    referencingProperty: primaryKeyInTarget ? mappingPropertyValue : keyPropertyValue,
                });
            });
    }
}

function addMappingReferences(
    references: AdminerReferences,
    mapping: MappingInit,
    kindMapping: MappingInit,
    kindProperties: Set<SimplePropertyFromServer>,
): AdminerReferences {
    const mappingPathProperties = getPropertiesFromAccessPath(mapping.accessPath, new Set<SimplePropertyFromServer>());
    const mappingPropertiesArray = Array.from(mappingPathProperties);
    const kindPropertiesArray = Array.from(kindProperties);

    addReferences(references, kindMapping.primaryKey, kindMapping, mapping, kindPropertiesArray, mappingPropertiesArray, true);

    addReferences(references, mapping.primaryKey, mapping, kindMapping, mappingPropertiesArray, kindPropertiesArray, false);

    return references;
}

async function getAllMappings(): Promise<MappingFromServer[]> {
    const mappingResponse = await api.mappings.getAllMappings({});

    if (!mappingResponse.status)
        throw new Error(`Failed to fetch mappings`);

    return mappingResponse.data;
}

async function getSchemaCategory(categoryId: string): Promise<SchemaCategoryFromServer> {
    const schemaCategoryResponse = await api.schemas.getCategory({ id: categoryId });

    if (!schemaCategoryResponse.status)
        throw new Error(`Failed to fetch schema categories`);

    return schemaCategoryResponse.data;
}

function extractAllKindMappings(mappings: MappingFromServer[]): MappingInit[] {
    return mappings.flatMap((mapping: MappingFromServer) => {
        let fromSubpath: ComplexPropertyFromServer | null = null;
        let toSubpath: ComplexPropertyFromServer | null = null;
        const otherSubpaths: ChildPropertyFromServer[] = [];

        for (const subpath of mapping.accessPath.subpaths) {
            if (subpath.name && (subpath.name as StaticNameFromServer).type === 'STATIC') {
                const nameValue = (subpath.name as StaticNameFromServer).value;
                if (nameValue.startsWith('_from.'))
                    fromSubpath = subpath as ComplexPropertyFromServer;
                else if (nameValue.startsWith('_to.'))
                    toSubpath = subpath as ComplexPropertyFromServer;
                else
                    otherSubpaths.push(subpath);
            }
            else {
                otherSubpaths.push(subpath);
            }
        }

        const newMappings: MappingInit[] = [];

        if (fromSubpath) {
            newMappings.push({
                categoryId: mapping.categoryId,
                datasourceId: mapping.datasourceId,
                rootObjectKey: mapping.rootObjectKey,
                primaryKey: mapping.primaryKey,
                kindName: (fromSubpath.name as StaticNameFromServer).value.replace(/^_from\./, ''),
                accessPath: {
                    name: { type: 'ANONYMOUS', value: '' },
                    signature: fromSubpath.signature,
                    subpaths: fromSubpath.subpaths || [],
                },
            });
        }

        if (toSubpath) {
            newMappings.push({
                categoryId: mapping.categoryId,
                datasourceId: mapping.datasourceId,
                rootObjectKey: mapping.rootObjectKey,
                primaryKey: mapping.primaryKey,
                kindName: (toSubpath.name as StaticNameFromServer).value.replace(/^_to\./, ''),
                accessPath: {
                    name: { type: 'ANONYMOUS', value: '' },
                    signature: toSubpath.signature,
                    subpaths: toSubpath.subpaths || [],
                },
            });
        }

        if (fromSubpath != null || toSubpath != null) {
            newMappings.push({
                categoryId: mapping.categoryId,
                datasourceId: mapping.datasourceId,
                rootObjectKey: mapping.rootObjectKey,
                primaryKey: mapping.primaryKey,
                kindName: mapping.kindName,
                accessPath: {
                    name: mapping.accessPath.name,
                    signature: mapping.accessPath.signature,
                    subpaths: otherSubpaths,
                },
            });
        }
        else {
            newMappings.push(mapping);
        }

        return newMappings;
    });
}

async function getSchemaCategoryReferences(datasourceId: Id, kindName: string): Promise<AdminerReferences> {
    const allMappings = await getAllMappings();
    const mappings: MappingInit[] = extractAllKindMappings(allMappings);

    const kindMappings = mappings.filter(mapping =>
        mapping.kindName === kindName &&
        mapping.datasourceId === datasourceId);

    if (kindMappings.length === 0)
        return [];

    const references: AdminerReferences = [];
    for (const kindMapping of kindMappings) {
        const schemaCategory = await getSchemaCategory(kindMapping.categoryId);

        const schemaCategoryMappings = mappings.filter(mapping =>
            mapping.categoryId === schemaCategory.id );

        const mappingPathProperties = getPropertiesFromAccessPath(kindMapping.accessPath, new Set<SimplePropertyFromServer>());

        for (const mapping of schemaCategoryMappings)
            addMappingReferences(references, mapping, kindMapping, mappingPathProperties);
    }

    return references;
}

async function getForeignKeys(datasourceId: Id, kindName: string): Promise<AdminerReferences> {
    const foreignKeysResponse = await api.adminer.getReferences({ datasourceId: datasourceId, kindName: kindName });

    if (!foreignKeysResponse.status)
        throw new Error(`Failed to fetch foreign keys`);

    return foreignKeysResponse.data;
}

export function useFetchReferences(state: AdminerFilterQueryState) {
    const [ loading, setLoading ] = useState<boolean>(true);
    const [ references, setReferences ] = useState<AdminerReferences>([]);

    useEffect(() => {
        (async () => {
            setLoading(true);

            try {
                const foreignKeys = await getForeignKeys(state.datasourceId!, state.kindName!);
                const schemaCategoryReferences = await getSchemaCategoryReferences(state.datasourceId!, state.kindName!);

                setReferences(foreignKeys.concat(schemaCategoryReferences));
            }
            catch (e) {
                console.log(e);
            }

            setLoading(false);
        })();
    }, [ state.datasourceId, state.kindName ]);

    return { references: references, referencesLoading: loading };
}
