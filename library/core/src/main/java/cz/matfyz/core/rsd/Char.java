/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd;

/**
 *
 * @author pavel.koupil
 */
public enum Char {
    FALSE, UNKNOWN, TRUE;

    public static Char min(Char a, Char b) {
        if (a == FALSE || b == FALSE) {
            return FALSE;
        }
        if (a == UNKNOWN || b == UNKNOWN) {
            return UNKNOWN;
        }
        return TRUE;
    }
}
