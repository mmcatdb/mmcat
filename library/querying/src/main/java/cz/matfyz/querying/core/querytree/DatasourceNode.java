package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.JoinCandidate.SerializedJoinCandidate;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.patterntree.PatternObject.SerializedPatternObject;
import cz.matfyz.querying.core.querytree.FilterNode.SerializedFilterNode;
import cz.matfyz.querying.parsing.Filter;
import cz.matfyz.querying.parsing.Term;

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
    public final SchemaCategory schema;
    public final List<JoinCandidate> joinCandidates;
    public final List<Filter> filters;
    /** The root term of this pattern. When this node is translated to query, this term will be the root of the query structure. */
    public final Term rootTerm;

    public DatasourceNode(
        Datasource datasource,
        Set<PatternForKind> kinds,
        SchemaCategory schema,
        List<JoinCandidate> joinCandidates,
        Term rootTerm
    ) {
        this.datasource = datasource;
        this.kinds = kinds;
        this.schema = schema;
        this.joinCandidates = joinCandidates;
        this.filters = List.of();
        this.rootTerm = rootTerm;
    }

    public DatasourceNode(
        Datasource datasource,
        Set<PatternForKind> kinds,
        SchemaCategory schema,
        List<JoinCandidate> joinCandidates,
        List<Filter> filters,
        Term rootTerm
    ) {
        this.datasource = datasource;
        this.kinds = kinds;
        this.schema = schema;
        this.joinCandidates = joinCandidates;
        this.filters = filters;
        this.rootTerm = rootTerm;
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public record SerializedDatasourceNode(
        String datasourceIdentifier,
        Map<String, SerializedPatternObject> kinds,
        List<SerializedJoinCandidate> joinCandidates,
        List<String> filters,
        String rootTerm
    ) implements SerializedQueryNode{

        @Override public String getType() { return "datasource"; }

    }
}
