package cz.matfyz.integration.exception;

import java.io.Serializable;

import org.apache.jena.graph.Node;

/**
 * @author jachymb.bartik
 */
public class ObjectException extends IntegrationException {
    
    private record ObjectData(
        String pimIri,
        Node node
    ) implements Serializable {}
    
    protected ObjectException(String name, ObjectData data) {
        super("object." + name, data, null);
    }

    public static ObjectException notFound(String pimIri) {
        return new ObjectException("notFound", new ObjectData(pimIri, null));
    }

    public static ObjectException idsIsNotValue(String pimIri, Node node) {
        return new ObjectException("idsIsNotValue", new ObjectData(pimIri, node));
    }

}
