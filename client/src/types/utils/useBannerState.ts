import { useState, useEffect } from 'react';
import { get, set } from '@/types/utils/localStorage';

function getBannerKey(page: string) {
    return `bannerState_${page}`;
}

// Check if a banner is dismissed for the current page
function isBannerDismissed(page: string) {
    return get<boolean>(getBannerKey(page)) ?? true;
}

// Set banner dismissal for the current page
function setBannerDismissed(page: string, dismissed: boolean) {
    set(getBannerKey(page), dismissed);
}

// Hook for managing banner state per page
export function useBannerState(page: string) {
    const [ isVisible, setIsVisible ] = useState(
        !isBannerDismissed(page),
    );

    useEffect(() => {
        setIsVisible(!isBannerDismissed(page));
    }, [ page ]);

    function dismissBanner() {
        setBannerDismissed(page, true);
        setIsVisible(false);
    }

    function restoreBanner() {
        setBannerDismissed(page, false);
        setIsVisible(true);
    }

    return { isVisible, dismissBanner, restoreBanner };
}
