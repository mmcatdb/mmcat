import { useLocalStorageBackup } from '@/types/utils/localStorage';

function getBannerKey(page: string) {
    return `bannerState_${page}`;
}

/** Hook for managing banner state per page. */
export function useBannerState(page: string) {
    const [ isDismissed, setIsDismissed ] = useLocalStorageBackup<boolean>(getBannerKey(page));

    return {
        isDismissed,
        setIsDismissed,
    };
}

export type UseBannerReturn = ReturnType<typeof useBannerState>;
