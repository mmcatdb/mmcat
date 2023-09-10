package cz.matfyz.querying.core.querytree;

public class MinusNode extends QueryNode {
 
    public final QueryNode primaryChild;
    public final QueryNode minusChild;

    public MinusNode(QueryNode primaryChild, QueryNode minusChild) {
        this.primaryChild = primaryChild;
        this.minusChild = minusChild;
        
        primaryChild.setParent(this);
        minusChild.setParent(this);
    }

}
