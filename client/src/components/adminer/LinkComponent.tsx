import { Link } from 'react-router-dom';
import { Tooltip } from '@nextui-org/react';
import { routes } from '@/routes/routes';
import { usePreferences } from '@/components/PreferencesProvider';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';
import type { Datasource } from '@/types/datasource/Datasource';

const NAME_LENGTH = 2;

type LinkComponentProps = Readonly<{
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
}>;

/**
 * Component that returns a link with tooltip
 */
export function LinkComponent({ index, reference, kind, datasourceId, datasources, link, kindDuplicated }: LinkComponentProps ) {
    const { preferences } = usePreferences();

    return (
        <Tooltip
            content={createLinkText(reference, datasourceId, datasources, kind, true, false)}
            color='primary'
        >
            <Link
                key={index}
                to={{ pathname:routes.adminer, search: link }}
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
