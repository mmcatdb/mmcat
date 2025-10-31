package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.JoinCandidate.SerializedJoinCandidate;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.patterntree.PatternTree.SerializedPatternTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A query node representing a part of the query evaluatable as a (probably single) query in a single datasource.
 * In the future, its structure may also be defined by a (sub-)tree, but so far DatasourceNodes are leaves in the query tree.
 */
public class DatasourceNode extends QueryNode {

    public final Datasource datasource;
    /** All kinds used in this pattern. */
    public final Set<PatternForKind> kinds;
    public final List<JoinCandidate> joinCandidates;
    public final ArrayList<Computation> filters;
    /** The root term of this pattern. When this node is translated to query, this term will be the root of the result structure. */
    public final Variable rootVariable;

    public DatasourceNode(
        Datasource datasource,
        Set<PatternForKind> kinds,
        List<JoinCandidate> joinCandidates,
        List<Computation> filters,
        Variable rootVariable
    ) {
        this.datasource = datasource;
        this.kinds = kinds;
        this.joinCandidates = joinCandidates;
        this.filters = new ArrayList<Computation>(filters);
        this.rootVariable = rootVariable;
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public record SerializedDatasourceNode(
        String datasourceIdentifier,
        Map<String, SerializedPatternTree> kinds,
        List<SerializedJoinCandidate> joinCandidates,
        List<String> filters,
        String rootVariable
    ) implements SerializedQueryNode{

        @Override public String getType() { return "datasource"; }

    }

}
