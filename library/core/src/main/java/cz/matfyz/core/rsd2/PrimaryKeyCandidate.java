/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
