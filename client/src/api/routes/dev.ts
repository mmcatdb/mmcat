import type { Empty } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';

export const devApi = {
    ping: GET<Empty, string>(
        () => '/ping',
    ),

    runTests: POST<Empty, string>(
        () => '/runTests',
    ),
};
