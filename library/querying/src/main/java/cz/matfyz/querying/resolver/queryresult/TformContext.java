package cz.matfyz.querying.resolver.queryresult;

import cz.matfyz.core.querying.ResultNode;
import cz.matfyz.core.querying.ResultNode.NodeBuilder;

import java.util.ArrayDeque;
import java.util.Deque;

public class TformContext {

    /**
     * The currently traversed nodes.
     */
    public final Deque<ResultNode> inputs = new ArrayDeque<>();

    /**
     * Outputs of steps.
     */
    public final Deque<ResultNode> outputs = new ArrayDeque<>();

    /**
     * The currently used builders.
     */
    public final Deque<NodeBuilder> builders = new ArrayDeque<>();

    /**
     * The currently used removers. Can be used to remove the whole subtree from the current remover context.
     */
    public final Deque<RemoverContext> removers = new ArrayDeque<>();

    public interface RemoverContext {

        void getRemoved();

    }

    public TformContext(ResultNode input) {
        inputs.push(input);
    }

    public ResultNode getOutput() {
        return outputs.pop();
    }

}
