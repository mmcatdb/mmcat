package cz.matfyz.querying.exception;

import cz.matfyz.querying.parsing.ParserNode;

import java.io.Serializable;

public class ParsingException extends QueryingException {

    protected ParsingException(String name, Serializable data) {
        super("parsing." + name, data, null);
    }

    private record WrongNodeData(
        String supposedType,
        ParserNode actualNode
    ) implements Serializable {}

    public static <T> ParsingException wrongNode(Class<T> supposedType, ParserNode actualNode) {
        return new ParsingException("wrongNode", new WrongNodeData(supposedType.getSimpleName(), actualNode));
    }

    public static ParsingException signature(String signatureString) {
        return new ParsingException("signature", signatureString);
    }

}
