import type { Empty } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { InstanceCategoryFromServer } from '@/types/instance';

const instances = {
    getInstanceCategory: GET<Empty, InstanceCategoryFromServer>(
        () => '/instances',
    ),
};

export default instances;
