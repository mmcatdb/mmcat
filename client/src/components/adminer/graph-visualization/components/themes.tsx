import { type JSX } from 'react';
import { ThemeProvider } from 'styled-components';

// These and colors in dark theme from light palette are to be translated to corresponding
// version in dark palette when available
const NDLColors = {
    neutral: {
        '70': {
            opacity50: 'rgb(113, 119, 128, 0.5)',
            opacity60: 'rgb(113, 119, 128, 0.6)',
        },
    },
};

const light = {
    name: 'light',

    // Text colors
    primaryText: '#333',
    link: '#428BCA',

    // Design system colors
    primary: '#018BFF',

    // Backgrounds
    primaryBackground: '#D2D5DA',
    editorBackground: '#fff',
    topicBackground: '#f8f8f8',
    preBackground: '#f5f5f5',
    alteringTableRowBackground: '#f5f5f5',
    frameCommandBackground: '#F8F9FB',
    runnableBackground: '#f5f5f5',
    teaserCardBackground: '#fff',
    hoverBackground: '#40444e',

    // Fonts
    primaryFontFamily: '"Helvetica Neue", Helvetica, Arial, sans-serif',
    drawerHeaderFontFamily: '\'Open Sans\', \'HelveticaNeue-Light\', \'Helvetica Neue Light\', \'Helvetica Neue\', Helvetica, Arial, sans-serif',

    // Shadows
    standardShadow: '0px 0px 2px rgba(52, 58, 67, 0.1), 0px 1px 2px rgba(52, 58, 67, 0.08), 0px 1px 4px rgba(52, 58, 67, 0.08);',

    // User feedback colors
    success: '#65B144',
    error: '#E74C3C',
    warning: '#ffaf00',
    auth: '#428BCA',
    info: '#428BCA',

    // Borders
    inFrameBorder: '1px solid #DAE4F0;',

    // Frame
    frameSidebarBackground: '#FFF',
    frameControlButtonTextColor: '#485662',
    // frameButtonTextColor: needlePalette.light.neutral.text.weaker,
    // frameButtonHoverBackground: needlePalette.light.neutral.hover,
    // frameButtonActiveBackground: needlePalette.light.neutral.pressed,
    frameNodePropertiesPanelIconTextColor: '#717172',
    frameBackground: '#F9FCFF',
};

const dark = {
    ...light,
    name: 'dark',

    primaryText: '#f4f4f4',
    link: '#5CA6D9',

    // Backgrounds
    primaryBackground: '#525865',
    editorBackground: '#121212',
    topicBackground: 'transparent',
    preBackground: '#282c32',
    alteringTableRowBackground: '#30333a',
    frameCommandBackground: '#31333B',
    runnableBackground: '#202226',
    teaserCardBackground: '#31333B',

    // Borders
    inFrameBorder: '1px solid rgba(255,255,255,0.12)',

    // Frame
    frameSidebarBackground: '#121212',
    frameControlButtonTextColor: '#D7E5F1',
    // frameButtonTextColor: needlePalette.light.neutral.bg.default,
    frameButtonHoverBackground: NDLColors.neutral['70'].opacity60,
    frameButtonActiveBackground: NDLColors.neutral['70'].opacity50,
    frameNodePropertiesPanelIconTextColor: '#f4f4f4',
    frameBackground: '#292C33',
};

const themes = {
    light,
    dark,
};

/**
 * Get Theme provider for Arc components, default is LIGHT theme.
 */
export function ArcThemeProvider({ children, theme }: { children: JSX.Element, theme?: 'light' | 'dark' }) {
    return (
        <ThemeProvider theme={themes[theme ?? 'dark']}>{children}</ThemeProvider>
    );
}
