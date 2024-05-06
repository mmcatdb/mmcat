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
public class ReferenceCandidate {

    final String type = "reference";
    @JsonIgnore
    transient Object subsetType;
    @JsonIgnore
    transient Object referredProperty;
    @JsonIgnore
    transient Object referencingProperty;
    String referred;
    String referencing;
    boolean weak;
    boolean selected;

    public String toString() {
        return "ReferenceCandidate{" + "type=" + type + ", referencing= " + referencing + ", referred=" + referred + ", weak=" + weak + ", selected=" + selected + '}';
    }
}

