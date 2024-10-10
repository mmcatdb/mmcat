/**
 * Enum representing different types of graph layouts.
 * @enum {string}
 * @property {string} FR - Force-directed layout.
 * @property {string} CIRCLE - Circular layout.
 * @property {string} KK - Kamada-Kawai layout.
 * @property {string} ISOM - Self-Organizing Map layout.
 */
export enum LayoutType {
    FR = 'FR',        // Force-directed
    CIRCLE = 'CIRCLE', // Circular
    KK = 'KK',        // Kamada-Kawai
    ISOM = 'ISOM',    // Self-Organizing Map
}
