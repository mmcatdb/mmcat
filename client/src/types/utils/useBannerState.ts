import { useMemo } from 'react';
import { useLocalStorageBackup } from '@/types/utils/localStorage';

function getBannerKey(page: string) {
    return `bannerState_${page}`;
}

/** Hook for managing banner state per page. */
export function useBannerState(page: string) {
    const [ isDismissed, setIsDismissed ] = useLocalStorageBackup<boolean>(getBannerKey(page));

    const { dismissBanner, restoreBanner } = useMemo(() => ({
        dismissBanner: () => setIsDismissed(true),
        restoreBanner: () => setIsDismissed(false),
    }), [ setIsDismissed ]);

    return {
        isVisible: !isDismissed,
        dismissBanner,
        restoreBanner,
    };
}
