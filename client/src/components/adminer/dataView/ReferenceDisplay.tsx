import { useState } from 'react';
import { Button, Tooltip } from '@heroui/react';
import { getHrefFromReference } from '@/components/adminer/URLParamsState';
import type { Datasource } from '@/types/Datasource';
import type { GraphResponseData } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';
import { usePreferences } from '@/components/context/PreferencesProvider';
import { Link } from 'react-router-dom';
import { routes } from '@/routes/routes';

type ReferenceDisplayProps = {
    /** The references to create links for. */
    references: KindReference[];
    /** The displayeddata. */
    data: Record<string, string> | GraphResponseData;
    /** The name of the property to create links for. */
    property: string;
    /** Name of current kind. */
    kind: string;
    /** The id of the current datasource. */
    datasourceId: Id;
    /** All active datasources. */
    datasources: Datasource[];
};

/**
 * Component for displaying references
 */
export function ReferenceDisplay({ references, data, property, kind, datasourceId, datasources }: ReferenceDisplayProps ) {
    const [ visible, setVisible ] = useState(false);
    const links = new Map<string, KindReference>();

    references
        .filter(ref => ref.fromProperty === property)
        .forEach(ref => {
            // Filter out the references to the actual property
            if (!(ref.datasourceId === datasourceId && ref.kindName === kind && ref.property === property)) {
                const link = getHrefFromReference(ref, data, property, datasources);
                if (!links.has(link))
                    links.set(link, ref);
            }
        });

    const linkEntries = [ ...links.entries() ].toSorted((a, b) => compareReferences(a[1], b[1], kind, datasourceId));
    const compressedLinks = getCompressedLinks(linkEntries);

    const visibleCount = Math.min(5, compressedLinks.length);
    const alwaysVisibleLinks = compressedLinks.slice(0, visibleCount);
    const hiddenLinks = compressedLinks.slice(visibleCount);

    return (<>
        {alwaysVisibleLinks.map(([ link, ref, duplicatedKind ]) => (
            <LinkDisplay
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
            <LinkDisplay
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
                className='mx-1 h-5 px-1 min-w-5'
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

const NAME_LENGTH = 2;

type LinkDisplayProps = {
    /** The id of the current link. */
    index: string;
    /** The reference to create the link for. */
    reference: KindReference;
    /** Name of current kind. */
    kind: string;
    /** The id of the current datasource. */
    datasourceId: Id;
    /** All active datasources. */
    datasources: Datasource[];
    /** The URL parameters to be added to the link. */
    link: string;
    /** 'true' if there exist another link that points to the same kind. */
    kindDuplicated: boolean;
};

/**
 * Component that returns a link with tooltip
 */
export function LinkDisplay({ index, reference, kind, datasourceId, datasources, link, kindDuplicated }: LinkDisplayProps ) {
    const { preferences } = usePreferences();

    return (
        <Tooltip
            content={createLinkText(reference, datasourceId, datasources, kind, true, false)}
            color='primary'
        >
            <Link
                key={index}
                to={{ pathname: routes.adminer, search: link }}
                className='mr-2 hover:underline text-blue-500'
            >
                {createLinkText(reference, datasourceId, datasources, kind, kindDuplicated, preferences.adminerShortLinks)}
            </Link>
        </Tooltip>
    );
}

function createLinkText(reference: KindReference, datasourceId: Id, datasources: Datasource[], kind: string, kindDuplicated: boolean, short: boolean) {
    const datasourceLabel: string = datasources.find(source => source.id === reference.datasourceId)!.label;
    const kindName = reference.kindName;
    const property = reference.property;

    let linkText = '';
    if (datasourceId !== reference.datasourceId)
        linkText += `${short ? datasourceLabel.substring(0, NAME_LENGTH) : datasourceLabel}/`;
    if (kind !== reference.kindName)
        linkText += `${short ? kindName.substring(0, NAME_LENGTH) : kindName}`;
    if (kindDuplicated)
        linkText += `:${short ? property.substring(0, NAME_LENGTH) : property}`;

    return linkText;
}
