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

// FIXME Toto se mi příliš nelíbí, protože to je velmi specifické pro neo4j. Jako tady je každá node či relationship de facto dokument, který může mít úplně libovolné věci (v závislosti na dané databázi). Spíš by mi dávalo smysl vytvořit nějaká "obecná" grafová data, která lze použít v každé databázi, a k nim přidat libovolná specifická data.
// Např. si řekntete, že graf má množinu nodes a relationships. Takže místo List<GraphElement> vrátíte List<Node> a List<Relationship>. Dále, každá Node bude mít nějaké id, zatímco relationship bude mít id a dále id obou nodes. Každý také může mít Map<String, Object> properties.
// No a na FE potom budete řešit, že konkrétní název "#elementId" (pro neo4j) odpovídá "id dané node" a podobně. Nicméně to myslím stačí řešit jen u filtračních funkcí a tak, ne nutně všude.

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
