/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd;

import java.util.List;

/**
 *
 * @author pavel.koupil, sebastian.hricko
 */
public class RedundancyCandidate {

    final String type = "redundancy";
    List<RedundancyPair> redundancyPairs;
    boolean full;
    boolean selected;

    public String toString() {
        return "RedundancyCandidate{" + "type=" + type + ", redundancyPairs=" + redundancyPairs + ", full=" + full + ", selected=" + selected + '}';
    }

}
