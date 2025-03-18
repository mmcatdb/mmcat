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

    return (
        <div>
            <Button
                className='my-1 h-5'
                variant='ghost'
                onPress={() => setVisible(!visible)}>
                {visible ? 'Hide references' : 'Show references'}
            </Button>

            {visible && Array.from(links.entries()).map(([ link, ref ]) => (
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
        </div>
    );
}
