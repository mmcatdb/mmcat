import { useState } from 'react';
import { Button } from '@nextui-org/react';
import { LinkComponent } from '@/components/adminer/LinkComponent';
import { getHrefFromReference } from '@/components/adminer/URLParamsState';
import type { Datasource } from '@/types/datasource/Datasource';
import type { GraphResponseData } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type ReferenceComponentProps = Readonly<{
    /** The references to create links for. */
    references: KindReference[];
    /** The displayeddata. */
    data: Record<string, string> | GraphResponseData;
    /** The name of the property to create links for. */
    propertyName: string;
    /** Name of current kind. */
    kind: string;
    /** The id of the current datasource. */
    datasourceId: Id;
    /** All active datasources. */
    datasources: Datasource[];
}>;

/**
 * Component for displaying references
 */
export function ReferenceComponent({ references, data, propertyName, kind, datasourceId, datasources }: ReferenceComponentProps ) {
    const [ visible, setVisible ] = useState(false);
    const links = new Map<string, KindReference>();

    references
        .filter(ref => ref.fromProperty === propertyName)
        .forEach(ref => {
            // Filter out the references to the actual property
            if (!(ref.datasourceId === datasourceId && ref.kindName === kind && ref.property === propertyName)) {
                const link = getHrefFromReference(ref, data, propertyName, datasources);
                if (!links.has(link))
                    links.set(link, ref);
            }
        });

    const linkEntries = Array.from(links.entries());
    linkEntries.sort((a, b) =>
        compareReferences(a[1], b[1], kind, datasourceId),
    );

    const compressedLinks = getCompressedLinks(linkEntries);

    const visibleCount = Math.min(5, compressedLinks.length);
    const alwaysVisibleLinks = compressedLinks.slice(0, visibleCount);
    const hiddenLinks = compressedLinks.slice(visibleCount);

    return (<>
        {alwaysVisibleLinks.map(([ link, ref, duplicatedKind ]) => (
            <LinkComponent
                key={link}
                index={link}
                reference={ref}
                kind={kind}
                datasourceId={datasourceId}
                datasources={datasources}
                link={link}
                kindDuplicated={duplicatedKind}
            />
        ))}

        {visible && hiddenLinks.map(([ link, ref, duplicatedKind ]) => (
            <LinkComponent
                key={link}
                index={link}
                reference={ref}
                kind={kind}
                datasourceId={datasourceId}
                datasources={datasources}
                link={link}
                kindDuplicated={duplicatedKind}
            />
        ))}

        {hiddenLinks.length > 0 && (
            <Button
                className='m-1 h-5 px-1 min-w-5'
                variant='solid'
                onPress={() => setVisible(!visible)}
            >
                {visible ? '-' : '+'}
            </Button>
        )}
    </>);
}

function compareReferences(a: KindReference, b: KindReference, kind: string, datasourceId: Id): number {
    const aInKind = isInKind(a, kind, datasourceId);
    const bInKind = isInKind(b, kind, datasourceId);
    if (aInKind !== bInKind)
        return aInKind ? -1 : 1;

    const aIsDatasource = isInDatasource(a, datasourceId);
    const bIsDatasource = isInDatasource(b, datasourceId);
    if (aIsDatasource !== bIsDatasource)
        return aIsDatasource ? -1 : 1;

    if (a.datasourceId !== b.datasourceId)
        return a.datasourceId.localeCompare(b.datasourceId);
    if (a.kindName !== b.kindName)
        return a.kindName.localeCompare(b.kindName);
    return a.property.localeCompare(b.property);
}

function isInKind(ref: KindReference, kind: string, datasourceId: Id): boolean {
    return ref.kindName === kind && ref.datasourceId === datasourceId;
}

function isInDatasource(ref: KindReference, datasourceId: Id): boolean {
    return ref.datasourceId === datasourceId;
}

function getCompressedLinks(links: [string, KindReference][]): [string, KindReference, boolean][] {
    return links.map((current, index) => {
        const [ currentLink, currentRef ] = current;

        const kindDuplicated = links.some(([ , ref ], i) =>
            i !== index &&
            ref.datasourceId === currentRef.datasourceId &&
            ref.kindName === currentRef.kindName,
        );

        return [ currentLink, currentRef, kindDuplicated ];
    });
}
