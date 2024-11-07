package cz.matfyz.core.rsd;

import java.io.Serializable;
import java.util.List;

public class CandidatesSerializer {

    public record SerializedCandidates(
        List<SerializedPrimaryKeyCandidate> pkCandidates,
        List<SerializedReferenceCandidate> refCandidates
    ) implements Serializable {}

    public static SerializedCandidates serialize(Candidates candidates) {
        List<SerializedPrimaryKeyCandidate> serializedPkCandidates = candidates.pkCandidates.stream()
            .map(SerializedPrimaryKeyCandidate::serialize)
            .toList();

        List<SerializedReferenceCandidate> serializedRefCandidates = candidates.refCandidates.stream()
            .map(SerializedReferenceCandidate::serialize)
            .toList();

        return new SerializedCandidates(
            serializedPkCandidates,
            serializedRefCandidates
        );
    }

    public static Candidates deserialize(SerializedCandidates serializedCandidates) {
        Candidates candidates = new Candidates();

        serializedCandidates.pkCandidates().stream()
            .map(SerializedPrimaryKeyCandidate::deserialize)
            .forEach(candidates.pkCandidates::add);

        serializedCandidates.refCandidates().stream()
            .map(SerializedReferenceCandidate::deserialize)
            .forEach(candidates.refCandidates::add);

        return candidates;
    }

    public record SerializedPrimaryKeyCandidate(
        String type,
        String hierarchicalName,
        boolean selected
    ) implements Serializable {

        public static SerializedPrimaryKeyCandidate serialize(PrimaryKeyCandidate candidate) {
            return new SerializedPrimaryKeyCandidate(
                candidate.type(),
                candidate.hierarchicalName(),
                candidate.selected()
            );
        }

        public PrimaryKeyCandidate deserialize() {
            return new PrimaryKeyCandidate(
                this.type(),
                null,
                this.hierarchicalName(),
                this.selected()
            );
        }
    }

    public record SerializedReferenceCandidate(
        String type,
        String referred,
        String referencing,
        boolean weak,
        boolean selected
    ) implements Serializable {

    public static SerializedReferenceCandidate serialize(ReferenceCandidate candidate) {
        return new SerializedReferenceCandidate(
            candidate.type(),
            candidate.referred(),
            candidate.referencing(),
            candidate.weak(),
            candidate.selected()
        );
    }

    public ReferenceCandidate deserialize() {
        return new ReferenceCandidate(
            null, // subsetType
            null, // referredProperty
            null, // referencingProperty
            this.referred(),
            this.referencing(),
            this.weak(),
            this.selected()
        );
    }
}


}
