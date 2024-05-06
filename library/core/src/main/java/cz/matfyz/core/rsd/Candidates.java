/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cz.matfyz.core.rsd;

import java.util.ArrayList;
import java.util.List;

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
public class Candidates {
    List<PrimaryKeyCandidate> pkCandidates = new ArrayList<>();
    List<ReferenceCandidate> refCandidates = new ArrayList<>();
    List<RedundancyCandidate> redCandidates = new ArrayList<>();

    @Override
    public String toString() {
        return "Candidates{" + "pkCandidates=" + pkCandidates + ", refCandidates=" + refCandidates + ", redCandidates=" + redCandidates + '}';
    }
}