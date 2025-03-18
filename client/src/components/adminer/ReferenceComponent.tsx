import { useState } from 'react';
import { Button } from '@nextui-org/react';
import { LinkComponent } from '@/components/adminer/LinkComponent';
import { getHrefFromReference } from '@/components/adminer/URLParamsState';
import type { Datasource } from '@/types/datasource/Datasource';
import type { GraphResponseData } from '@/types/adminer/DataResponse';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type ReferenceComponentProps = Readonly<{
    references: KindReference[];
    data: Record<string, string> | GraphResponseData;
    propertyName: string;
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
}>;

export function ReferenceComponent({ references, data, propertyName, kind, datasourceId, datasources }: ReferenceComponentProps ) {
    const [ visible, setVisible ] = useState(false);
    const links = new Map<string, KindReference>();

    references
        .filter(ref => ref.referencingProperty === propertyName)
        .forEach(ref => {
            const link = getHrefFromReference(ref, data, propertyName, datasources);
            if (!links.has(link))
                links.set(link, ref);
        });

    const linkEntries = Array.from(links.entries());
    const visibleCount = Math.min(5, linkEntries.length);
    const alwaysVisibleLinks = linkEntries.slice(0, visibleCount);
    const hiddenLinks = linkEntries.slice(visibleCount);

    return (
        <div>
            {alwaysVisibleLinks.map(([ link, ref ]) => (
                <LinkComponent
                    key={link}
                    index={link}
                    reference={ref}
                    kind={kind}
                    datasourceId={datasourceId}
                    datasources={datasources}
                    link={link}
                />
            ))}

            {visible && hiddenLinks.map(([ link, ref ]) => (
                <LinkComponent
                    key={link}
                    index={link}
                    reference={ref}
                    kind={kind}
                    datasourceId={datasourceId}
                    datasources={datasources}
                    link={link}
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
        </div>
    );
}
