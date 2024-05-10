/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class PrimaryKeyCandidate {
    final String type = "primary";
    @JsonIgnore
    transient Object property;
    String hierarchicalName;
    boolean selected;

    public String toString() {
        return "PrimaryKeyCandidate{" + "type=" + type + ", hierarchicalName=" + hierarchicalName + ", selected=" + selected + '}';
    }
}
