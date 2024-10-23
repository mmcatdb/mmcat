import { Portal } from '@/components/common';
import { BreadcrumbItem, Breadcrumbs } from '@nextui-org/react';
import { Link, useParams } from 'react-router-dom';

export const ModelsPage = () => {
    const { projectId } = useParams<'projectId'>();

    return (
        <div>
            <Portal to='breadcrumb-portal'>
                <Breadcrumbs className='breadcrumb'>
                    <BreadcrumbItem>
                        <Link to='/schema-categories' >
                            Schema Categories
                        </Link>
                    </BreadcrumbItem>
                    <BreadcrumbItem>
                        <Link to={`/projects/${projectId}`} >
                            Project {projectId}
                        </Link>
                    </BreadcrumbItem>
                    <BreadcrumbItem>
                        <Link to={`/projects/${projectId}/models`}>
                            <span>Models</span>
                        </Link>
                    </BreadcrumbItem>
                </Breadcrumbs>
            </Portal>

            <h2>Models for Project {projectId}</h2>
            <p>Template page</p>
        </div>
    );
};
