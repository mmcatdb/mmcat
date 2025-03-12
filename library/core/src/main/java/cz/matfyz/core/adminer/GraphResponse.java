package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a graph response.
 */
public class GraphResponse extends DataResponse {
    private List<GraphElement> data;

    public GraphResponse(List<GraphElement> data, int itemCount, Set<String> propertyNames){
        super(itemCount, propertyNames);
        this.data = data;
    }

    public List<GraphElement> getData() {
        return data;
    }

    public interface GraphElement {
        String elementId();
        Map<String, Object> properties();
    }

    public record GraphNode(String elementId, Map<String, Object> properties) implements GraphElement {}

    public record GraphRelationship(String elementId, Map<String, Object> properties, String startNodeId, String endNodeId) implements GraphElement {}
}
