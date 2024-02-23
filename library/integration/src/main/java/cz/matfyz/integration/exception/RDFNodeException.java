package cz.matfyz.integration.exception;

import org.apache.jena.graph.Node;

/**
 * @author jachymb.bartik
 */
public class RDFNodeException extends IntegrationException {

    protected RDFNodeException(String name, Node node) {
        super("object." + name, node.toString(), null);
    }

    public static RDFNodeException unprocessable(Node node) {
        return new RDFNodeException("unprocessable", node);
    }

}
