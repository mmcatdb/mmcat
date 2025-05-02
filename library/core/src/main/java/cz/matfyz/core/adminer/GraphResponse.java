package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Map;

/**
 * Represents a graph response.
 */
public class GraphResponse extends DataResponse {
    private GraphData data;
    private static final String TYPE = "graph";

    public GraphResponse(GraphData data, int itemCount, List<String> propertyNames){
        super(itemCount, propertyNames);
        this.data = data;
    }

    public GraphData getData() {
        return data;
    }

    public record GraphData (
        List<GraphNode> nodes,
        List<GraphRelationship> relationships
    ) {}

    public interface GraphElement {
        String id();
        Map<String, Object> properties();
    }

    public record GraphNode(
        String id,
        Map<String, Object> properties
    ) implements GraphElement {}

    public record GraphRelationship(
        String id,
        String fromNodeId,
        String toNodeId,
        Map<String, Object> properties
    ) implements GraphElement {}

    @Override public String getType() {
        return TYPE;
    }

}
