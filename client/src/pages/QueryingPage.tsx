import { Portal } from '@/components/common';
import { Breadcrumbs, BreadcrumbItem } from '@nextui-org/react';
import { Link, useParams } from 'react-router-dom';

export const QueryingPage = () => {
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
                        <Link to={`/projects/${projectId}/querying`}>
                            <span>Querying</span>
                        </Link>
                    </BreadcrumbItem>
                </Breadcrumbs>
            </Portal>

            <h2>Querying for Project {projectId}</h2>
            <p>Template page</p>
        </div>
    );
};
