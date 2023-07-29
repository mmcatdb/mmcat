package cz.cuni.matfyz.querying.exception;

import cz.cuni.matfyz.querying.parsing.QueryNode;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class ParsingException extends QueryingException {

    protected ParsingException(String name, Serializable data) {
        super("parsing." + name, data, null);
    }

    private record WrongNodeData(
        String supposedType,
        QueryNode actualNode
    ) implements Serializable {}

    public static <T> ParsingException wrongNode(Class<T> supposedType, QueryNode actualNode) {
        return new ParsingException("wrongNode", new WrongNodeData(supposedType.getSimpleName(), actualNode));
    }

    public static ParsingException signature(String signatureString) {
        return new ParsingException("signature", signatureString);
    }

}
