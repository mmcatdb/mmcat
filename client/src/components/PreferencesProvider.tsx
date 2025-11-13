import { createContext, type ReactNode, useCallback, useContext, useEffect, useState } from 'react';
import { localStorage } from '@/types/utils/localStorage';

/**
 * Key for storing preferences in local storage.
 */
const PREFERENCES_KEY = 'preferences';

export type Theme = 'dark' | 'light';

/**
 * User preferences for the application.
 */
type Preferences = {
    theme: Theme;
    isCollapsed: boolean;
    showTableIDs: boolean;
    accessPathShortForm: boolean;
    adminerShortLinks: boolean;
};

type PreferencesContext = {
    preferences: Preferences;
    setPreferences: (preferences: Preferences) => void;
};

/**
 * Loads preferences from local storage with defaults.
 */
function fromStored(): Preferences {
    const stored = localStorage.get<Partial<Preferences>>(PREFERENCES_KEY) ?? {};

    return {
        theme: stored.theme ?? 'light',
        isCollapsed: stored.isCollapsed ?? false,
        showTableIDs: stored.showTableIDs ?? false,
        accessPathShortForm: stored.accessPathShortForm ?? true,
        adminerShortLinks: stored.adminerShortLinks ?? false,
    };
}

const defaultPreferences = fromStored();

/*
 * Converts preferences to a format suitable for local storage.
 */
function toStored(preferences: Preferences): Preferences {
    return {
        theme: preferences.theme,
        isCollapsed: preferences.isCollapsed,
        showTableIDs: preferences.showTableIDs,
        accessPathShortForm: preferences.accessPathShortForm,
        adminerShortLinks: preferences.adminerShortLinks,
    };
}

/**
 * Context for providing preferences to child components.
 */
export const PreferencesContext = createContext<PreferencesContext | undefined>(undefined);

/**
 * Provides user preferences to child components via context, with local storage persistence.
 */
export function PreferencesProvider({ children }: { children: ReactNode }) {
    const [ preferences, setPreferences ] = useState(defaultPreferences);

    const setPreferencesWithStorage = useCallback((preferences: Preferences) => {
        localStorage.set(PREFERENCES_KEY, toStored(preferences));
        setPreferences(preferences);
    }, []);

    const isDark = preferences.theme === 'dark';

    useEffect(() => {
        document.documentElement.classList.toggle('dark', isDark);
        document.documentElement.classList.toggle('light', !isDark);
    }, [ isDark ]);

    return (
        <PreferencesContext.Provider value={{ preferences, setPreferences: setPreferencesWithStorage }}>
            {children}
        </PreferencesContext.Provider>
    );
}

/**
 * Hook to access preferences from context.
 */
export function usePreferences(): PreferencesContext {
    const context = useContext(PreferencesContext);
    if (context === undefined)
        throw new Error('usePreferences must be used within an PreferencesProvider');

    return context;
}
