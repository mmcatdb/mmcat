import { createContext, useCallback, useContext, useState } from 'react';
import { localStorage } from '@/types/utils/localStorage';

const PREFERENCES_KEY = 'preferences';

export type Theme = 'dark' | 'light';

type Preferences = {
    theme: Theme;
    isCollapsed: boolean;
    showTableIDs: boolean;
};

type PreferencesContext = {
    preferences: Preferences;
    setPreferences: (preferences: Preferences) => void;
};

type StoredPreferences = {
    theme: Theme;
    isCollapsed: boolean;
    showTableIDs: boolean;
};

function fromStored(): Preferences {
    const stored = localStorage.get<Partial<StoredPreferences>>(PREFERENCES_KEY) ?? {};

    return {
        theme: stored.theme ?? 'dark',
        isCollapsed: stored.isCollapsed ?? false,
        showTableIDs: stored.showTableIDs ?? false,
    };
}

const defaultPreferences = fromStored();

function toStored(preferences: Preferences): StoredPreferences {
    return {
        theme: preferences.theme,
        isCollapsed: preferences.isCollapsed,
        showTableIDs: preferences.showTableIDs,
    };
}

export const PreferencesContext = createContext<PreferencesContext | undefined>(undefined);

export function PreferencesProvider({ children }: Readonly<{ children: React.ReactNode }>) {
    const [ preferences, setPreferences ] = useState(defaultPreferences);

    const setPreferencesWithStorage = useCallback((preferences: Preferences) => {
        localStorage.set(PREFERENCES_KEY, toStored(preferences));
        setPreferences(preferences);
    }, []);

    return (
        <PreferencesContext.Provider value={{ preferences, setPreferences: setPreferencesWithStorage }}>
            {children}
        </PreferencesContext.Provider>
    );
}

export function usePreferences(): PreferencesContext {
    const context = useContext(PreferencesContext);
    if (context === undefined)
        throw new Error('usePreferences must be used within an PreferencesProvider');

    return context;
}
