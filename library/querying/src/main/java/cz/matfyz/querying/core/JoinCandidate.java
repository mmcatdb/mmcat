package cz.matfyz.querying.core;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils.Edge;
import cz.matfyz.querying.core.patterntree.PatternForKind;

import java.io.Serializable;

public record JoinCandidate(
    JoinType type,
    /** The kind with the id (if we join as IdRef). */
    PatternForKind from,
    /** The kind with the reference (if we join as IdRef). */
    PatternForKind to,
    Variable variable,
    int recursion, // Some DBs allow to recursively join the same kind.
    boolean isOptional
) implements Edge<PatternForKind> {

    public enum JoinType {
        IdRef,
        Value,
    }

    public boolean isRecursive() {
        return recursion > 0;
    }

    public SerializedJoinCandidate serialize() {
        return new SerializedJoinCandidate(
            type,
            SerializedJoinCandidateKind.fromPatternForKind(from, variable),
            SerializedJoinCandidateKind.fromPatternForKind(to, variable),
            variable.name(),
            recursion,
            isOptional
        );
    }

    public record SerializedJoinCandidate(
        JoinType type,
        SerializedJoinCandidateKind from,
        SerializedJoinCandidateKind to,
        String variable,
        int recursion,
        boolean isOptional
    ) implements Serializable {}

    public record SerializedJoinCandidateKind(
        String kindName,
        String datasourceIdentifier,
        Signature path
    ) implements Serializable {

        static SerializedJoinCandidateKind fromPatternForKind(PatternForKind pattern, Variable variable) {
            return new SerializedJoinCandidateKind(
                pattern.kind.kindName(),
                pattern.kind.datasource().identifier,
                pattern.getPatternTree(variable).computePathFromRoot()
            );
        }

    }

}
