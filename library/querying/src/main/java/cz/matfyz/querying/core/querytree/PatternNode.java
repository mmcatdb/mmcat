package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.JoinCandidate.SerializedJoinCandidate;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.patterntree.PatternObject.SerializedPatternObject;
import cz.matfyz.querying.parsing.Term;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the pattern part of a query part.
 * It contains triples and other data that are used by other elements of the query part (i.e., by filters).
 * Instances of this class (and this class only) are leaves in the query tree.
 */
public class PatternNode extends QueryNode {

    /** All kinds used in this pattern. */
    public final Set<PatternForKind> kinds;
    public final SchemaCategory schema;
    public final List<JoinCandidate> joinCandidates;
    /** The root term of this pattern. When this node is translated to query, this term will be the root of the query structure. */
    public final Term rootTerm;

    public PatternNode(Set<PatternForKind> kinds, SchemaCategory schema, List<JoinCandidate> joinCandidates, Term rootTerm) {
        this.kinds = kinds;
        this.schema = schema;
        this.joinCandidates = joinCandidates;
        this.rootTerm = rootTerm;
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public record SerializedPatternNode(
        Map<String, SerializedPatternObject> kinds,
        List<SerializedJoinCandidate> joinCandidates,
        String rootTerm
    ) implements SerializedQueryNode{

        @Override public String getType() { return "pattern"; }

    }

}
