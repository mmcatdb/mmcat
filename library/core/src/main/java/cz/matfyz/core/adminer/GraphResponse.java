package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Map;

/**
 * Represents a graph response.
 */
public class GraphResponse extends DataResponse {
    private GraphData data;

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
        String startNodeId,
        String endNodeId,
        Map<String, Object> properties
    ) implements GraphElement {}

}
