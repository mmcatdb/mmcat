import { routes } from '@/routes/routes';
import { JOB_PAYLOAD_TYPES, JobPayloadType, type JobPayload } from '@/types/job';
import { Link } from 'react-router-dom';
import { useCategoryInfo } from '../CategoryInfoProvider';
import { Fragment } from 'react/jsx-runtime';

type JobPayloadDisplayProps = {
    payload: JobPayload;
};

// TODO maybe use this also on the action detail page
export function JobPayloadDisplay({ payload }: JobPayloadDisplayProps) {
    const { category } = useCategoryInfo();

    const label = (
        <div>
            <span className='font-bold'>{JOB_PAYLOAD_TYPES[payload.type].label}</span>
        </div>
    );

    if (payload.type === JobPayloadType.SchemaEvolution) {
        return (<>
            {label}
            <div>
                <span className='mr-1 font-bold'>Prev version:</span>
                {payload.prevVersion}
            </div>
            <div>
                <span className='mr-1 font-bold'>Next version:</span>
                {payload.nextVersion}
            </div>
        </>);
    }

    if (payload.type === JobPayloadType.ModelToCategory || payload.type === JobPayloadType.CategoryToModel) {
        return (<>
            {label}

            <div>
                <span className='mr-1 font-bold'>Datasource:</span>

                <Link
                    to={routes.category.datasources.detail.resolve({ categoryId: category.id, datasourceId: payload.datasource.id })}
                    className='text-primary-500 hover:underline'
                >
                    {payload.datasource.label}
                </Link>
            </div>

            {payload.mappings.length > 0 && (
                <div>
                    <span className='mr-1 font-bold'>Mappings:</span>

                    {payload.mappings.map((mapping, index) => (
                        <Fragment key={mapping.id}>
                            <Link
                                to={routes.category.mapping.resolve({ categoryId: category.id, mappingId: mapping.id })}
                                className='text-primary-500 hover:underline'
                            >
                                {mapping.kindName}
                            </Link>
                            {index !== payload.mappings.length - 1 && ', '}
                        </Fragment>
                    ))}
                </div>
            )}
        </>);
    }

    return (<>
        {label}

        <div>
            <span className='mr-1 font-bold'>Datasources:</span>

            {payload.datasources.map((datasource, index) => (
                <Fragment key={datasource.id}>
                    <Link
                        to={routes.category.datasources.detail.resolve({ categoryId: category.id, datasourceId: datasource.id })}
                        className='text-primary-500 hover:underline'
                    >
                        {datasource.label}
                    </Link>
                    {index !== payload.datasources.length - 1 && ', '}
                </Fragment>
            ))}
        </div>
    </>);
}
