package cz.matfyz.querying.core.querytree;

public class MinusNode extends OperationNode {
 
    @Override
    public Operation operation() {
        return Operation.Minus;
    }

    public MinusNode(GroupNode group) {
        super(group);
    }

    @Override
    public MinusNode updateGroup(GroupNode group) {
        return new MinusNode(group);
    }

}
