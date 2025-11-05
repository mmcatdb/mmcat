import type { Empty } from '@/types/api/routes';
import { GET } from '../routeFunctions';

export const devApi = {
    ping: GET<Empty, string>(
        () => '/ping',
    ),
};
