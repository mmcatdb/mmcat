import { Link } from 'react-router-dom';
import { routes } from '@/routes/routes';
import type { KindReference } from '@/types/adminer/AdminerReferences';
import type { Id } from '@/types/id';

type LinkComponentProps = Readonly<{
    index: string;
    reference: KindReference;
    kind: string;
    datasourceId: Id;
    link: string;
}>;

export function LinkComponent({ index, reference, kind, datasourceId, link }: LinkComponentProps ) {
    let linkText = '';
    if (datasourceId !== reference.datasourceId)
        linkText += `${reference.datasourceId}/`;
    if (kind !== reference.kindName)
        linkText += `${reference.kindName}:`;
    linkText += `${reference.property}`;

    return (
        <div>
            <Link
                key={index}
                to={{ pathname:routes.adminer, search: link }}
                className='mr-2 hover:underline text-blue-500'
            >
                {linkText}
            </Link>
        </div>
    );
}
