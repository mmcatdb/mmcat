package cz.matfyz.abstractwrappers.queryresult;

public class ResultLeaf implements ResultNode {

    public final String value;

    public ResultLeaf(String value) {
        this.value = value;
    }
    
}