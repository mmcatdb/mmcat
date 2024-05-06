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
 * @author sebastian.hricko
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RedundancyPair {
    @JsonIgnore
    transient Object property1;
    @JsonIgnore
    transient Object property2;
    String hierarchicalName1;
    String hierarchicalName2;

    public String toString() {
        return "RedundancyCandidate{" + ", hierarchicalName1=" + hierarchicalName1 + ", hierarchicalName2=" + hierarchicalName2 + '}';
    }
}