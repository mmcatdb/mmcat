package cz.matfyz.querying.core;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils.Edge;
import cz.matfyz.querying.core.patterntree.PatternForKind;

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
            from.kind.kindName(),
            to.kind.kindName(),
            variable.name(),
            from.getPatternTree(variable).computePathFromRoot(),
            to.getPatternTree(variable).computePathFromRoot(),
            recursion,
            isOptional
        );
    }

    public record SerializedJoinCandidate(
        JoinType type,
        String fromKind,
        String toKind,
        String variable,
        Signature fromPath,
        Signature toPath,
        int recursion,
        boolean isOptional
    ) {}

}
