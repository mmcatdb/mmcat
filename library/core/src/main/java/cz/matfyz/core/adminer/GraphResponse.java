package cz.matfyz.core.adminer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public record GraphNode(
        @JsonProperty("#elementId") String elementId,
        @JsonProperty("properties") Map<String, Object> properties,
        @JsonProperty("#labels") List<String> labels
    ) implements GraphElement {}

    public record GraphRelationship(
        @JsonProperty("#elementId")  String elementId,
        @JsonProperty("properties") Map<String, Object> properties,
        @JsonProperty("#startNodeId") String startNodeId,
        @JsonProperty("#endNodeId") String endNodeId
    ) implements GraphElement {}

    public record GraphRelationshipExtended(
        @JsonProperty("#elementId")  String elementId,
        @JsonProperty("properties") Map<String, Object> properties,
        @JsonProperty("#startNodeId") String startNodeId,
        @JsonProperty("startNode") Map<String, Object> startNode,
        @JsonProperty("#labelsStartNode") List<String> labelsStartNode,
        @JsonProperty("#endNodeId") String endNodeId,
        @JsonProperty("endNode") Map<String, Object> endNode,
        @JsonProperty("#labelsEndNode") List<String> labelsEndNode
    ) implements GraphElement {}
}
