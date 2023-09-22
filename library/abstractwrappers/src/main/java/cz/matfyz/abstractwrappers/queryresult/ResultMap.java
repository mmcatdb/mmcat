package cz.matfyz.abstractwrappers.queryresult;

import java.util.Map;

public class ResultMap implements ResultNode {

    public final Map<String, ResultNode> children;

    public ResultMap(Map<String, ResultNode> children) {
        this.children = children;
    }

}