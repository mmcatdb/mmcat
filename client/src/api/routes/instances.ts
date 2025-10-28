import type { Empty } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { InstanceCategoryResponse } from '@/types/instance';

export const instancesApi = {
    getInstanceCategory: GET<Empty, InstanceCategoryResponse>(
        () => '/instances',
    ),
};
