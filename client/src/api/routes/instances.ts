import type { Empty } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { InstanceCategoryResponse } from '@/types/instance';

const instances = {
    getInstanceCategory: GET<Empty, InstanceCategoryResponse>(
        () => '/instances',
    ),
};

export default instances;
