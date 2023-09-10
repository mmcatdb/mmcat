package cz.matfyz.querying.core.querytree;

import cz.matfyz.querying.core.JoinCandidate;

public class JoinNode extends OperationNode {
    
    @Override
    public Operation operation() {
        return Operation.Join;
    }

    public final JoinCandidate candidate;

    public JoinNode(GroupNode group, JoinCandidate candidate) {
        super(group);
        this.candidate = candidate;
    }

    @Override
    public JoinNode updateGroup(GroupNode group) {
        return new JoinNode(group, candidate);
    }

}
