import { Link } from 'react-router-dom';
import { routes } from '@/routes/routes';
import { usePreferences } from '@/components/PreferencesProvider';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';
import type { Datasource } from '@/types/datasource/Datasource';

const NAME_LENGTH = 2;

type LinkComponentProps = Readonly<{
    index: string;
    reference: KindReference;
    kind: string;
    datasourceId: Id;
    datasources: Datasource[];
    link: string;
}>;

function createLinkText(reference: KindReference, datasourceId: Id, datasources: Datasource[], kind: string, short: boolean) {
    const datasourceLabel: string = datasources.find(source => source.id === reference.datasourceId)!.label;
    const kindName = reference.kindName;
    const property = reference.property;

    let linkText = '';
    if (datasourceId !== reference.datasourceId)
        linkText += `${short ? datasourceLabel.substring(0, NAME_LENGTH) : datasourceLabel}/`;
    if (kind !== reference.kindName)
        linkText += `${short ? kindName.substring(0, NAME_LENGTH) : kindName}:`;
    linkText += `${short ? property.substring(0, NAME_LENGTH) : property}`;

    return linkText;
}

export function LinkComponent({ index, reference, kind, datasourceId, datasources, link }: LinkComponentProps ) {
    const { preferences } = usePreferences();

    return (
        <div>
            <Link
                key={index}
                to={{ pathname:routes.adminer, search: link }}
                className='mr-2 hover:underline text-blue-500'
            >
                {createLinkText(reference, datasourceId, datasources, kind, preferences.adminerShortLinks)}
            </Link>
        </div>
    );
}
