package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStructure;
import cz.matfyz.abstractwrappers.queryresult.QueryResult;
import cz.matfyz.abstractwrappers.queryresult.ResultLeaf;
import cz.matfyz.abstractwrappers.queryresult.ResultList;
import cz.matfyz.abstractwrappers.queryresult.ResultMap;
import cz.matfyz.abstractwrappers.queryresult.ResultNode;
import cz.matfyz.abstractwrappers.queryresult.ResultNode.NodeBuilder;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.core.utils.LineStringBuilder;
import cz.matfyz.core.utils.GraphUtils.Edge;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.exception.ProjectionException;
import cz.matfyz.querying.parsing.Aggregation;
import cz.matfyz.querying.parsing.SelectClause;
import cz.matfyz.querying.parsing.SelectTriple;
import cz.matfyz.querying.parsing.Variable;
import cz.matfyz.querying.parsing.ParserNode.Term;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class QueryProjector {

    public static ResultList run(QueryContext context, SelectClause selectClause, QueryResult selection) {
        return new QueryProjector(context, selectClause, selection).run();
    }

    private final QueryContext context;
    private final SelectClause selectClause;
    private final QueryResult selection;

    private QueryProjector(QueryContext context, SelectClause selectClause, QueryResult selection) {
        this.context = context;
        this.selectClause = selectClause;
        this.selection = selection;
    }

    
    private ResultList run() {
        final QueryStructure projectionStructure = computeProjectionStructure();
        final TransformationRoot transformation = computeTransformationSchema(projectionStructure);
        final var transformationContext = new TransformationContext(selection.data);
        
        String s = transformation.toString();
        System.out.println(s);

        System.out.println(selection.structure);
        System.out.println(projectionStructure);

        transformation.apply(transformationContext);

        return (ResultList) transformationContext.getOutput();
    }
    
    private QueryStructure computeProjectionStructure() {
        final var rootVariable = findRootVariable();
        final var rootObject = context.getObject(rootVariable);
        final var projectionStructure = new QueryStructure(rootVariable.getIdentifier(), rootObject, true);
        addChildrenToStructure(rootVariable, projectionStructure);

        return projectionStructure;
    }

    private record ProjectionEdge(Variable from, Term to, SelectTriple triple) implements Edge<Term> {}

    private Variable findRootVariable() {
        final var edges = selectClause.triples.stream().map(t -> new ProjectionEdge(t.subject, t.object, t)).toList();
        final var components = GraphUtils.findComponents(edges);
        if (components.size() != 1)
            throw ProjectionException.notSingleComponent();

        final var roots = GraphUtils.findRoots(components.iterator().next());
        if (roots.size() != 1) {
            final var objects = roots.stream().map(node -> {
                if (node instanceof Variable variable)
                    return context.getObject(variable);
                else if (node instanceof Aggregation aggregation)
                    return context.getObject(aggregation.variable);
                else
                    // Select clause can't contain constants.
                    throw new UnsupportedOperationException("Unsupported node type: " + node.getClass().getName());
            }).toList();

            throw ProjectionException.notSingleRoot(objects);
        }

        return roots.iterator().next().asVariable();
    }

    private void addChildrenToStructure(Variable parentVariable, QueryStructure parentStructure) {
        selectClause.triples.stream().filter(t -> t.subject.equals(parentVariable)).forEach(t -> {
            if (!(t.object instanceof Variable childVariable))
                // TODO it can be aggregation - fix it later.
                throw new UnsupportedOperationException("Object in selection is not variable!");

            // TODO we actually don't know if it's an array
            // We have to figure it out from the old structure - if there is an array on the path from the root to the parent, then it's an array.
            final var childStructure = new QueryStructure(t.name, context.getObject(childVariable), false);
            parentStructure.addChild(childStructure);
            addChildrenToStructure(childVariable, childStructure);
        });
    }

    private TransformationRoot computeTransformationSchema(QueryStructure projectionStructure) {
        final QueryStructure rootInSelection = GraphUtils.findDFS(selection.structure, (s) -> projectionStructure.object.equals(s.object));

        // TODO proper exception
        if (rootInSelection == null)
            throw new UnsupportedOperationException();

        final var root = new TransformationRoot();
        TransformationStep current = root
            .addChild(new CreateList<ResultMap>())
            .addChild(new TraverseList())
            .addChild(new WriteToList<ResultMap>());;

        current = addPathSteps(current, selection.structure, rootInSelection);
        addChildTransformation(current, projectionStructure);

        return root;
    }

    /**
     * Both source and target have to be in the selection structure.
     */
    private TransformationStep addPathSteps(TransformationStep current, QueryStructure source, QueryStructure target) {
        final var path = GraphUtils.findPath(source, target);
        // We ignore the last element because we don't want to travel from it.
        for (int i = 0; i < path.sourceToRoot().size() - 1; i++) {
            current = current.addChild(new TraverseParent());
            if (path.sourceToRoot().get(i).isArray)
                current = current.addChild(new TraverseParent());
        }

        // For each element, we travel to it. Therefore we skip the root element - we are already there, no need to travel.
        for (int i = 1; i < path.rootToTarget().size() - 1; i++) {
            final var structure = path.rootToTarget().get(i);
            current = current.addChild(new TraverseMap(structure.name));
            if (structure.isArray)
                current = current.addChild(new TraverseList());
        }

        if (path.rootToTarget().size() > 1) {
            final var targetStructure = path.rootToTarget().get(path.rootToTarget().size() - 1);
            current = current.addChild(new TraverseMap(targetStructure.name));
        }

        return current;
    }

    // Let A, B, ... be a QueryStructure, the [] symbol mens it has isArray = true and the -> symbol means its children. Then:
    //  - A[] -> B? : CreateList<ResultMap>, then CreateMap
    //  - A   -> B? : CreateMap
    //  - A[]       : CreateList<ResultLeaf>, then CreateLeaf
    //  - A         : CreateLeaf
    // The list of lists is not supported.
    private void addChildTransformation(TransformationStep current, QueryStructure childStructure) {
        if (childStructure.children.isEmpty())
            current.addChild(new CreateLeaf());
        else
            createMap(current, childStructure);
    }

    private TransformationStep addListIfNeeded(TransformationStep parentStep, QueryStructure structure) {
        if (!structure.isArray)
            return parentStep;

        if (structure.children.isEmpty()) {
            return parentStep
                .addChild(new CreateList<ResultLeaf>())
                .addChild(new TraverseList())
                .addChild(new WriteToList<ResultLeaf>());
        }

        return parentStep
            .addChild(new CreateList<ResultMap>())
            .addChild(new TraverseList())
            .addChild(new WriteToList<ResultMap>());
    }

    private void createMap(TransformationStep parentStep, QueryStructure structure) {
        final var mapStep = parentStep.addChild(new CreateMap());
        structure.children.values().forEach(childStructure -> {
            var current = mapStep
                // .addChild(new TraverseMap(childStructure.name))
                .addChild(new WriteToMap(childStructure.name));

            // final var parentInSelection = GraphUtils.findDFS(selection.structure, (s) -> s.name.equals(structure.name));
            // final var childInSelection = GraphUtils.findDFS(selection.structure, (s) -> s.name.equals(childStructure.name));
            final var parentInSelection = GraphUtils.findDFS(selection.structure, (s) -> s.object.equals(structure.object));
            final var childInSelection = GraphUtils.findDFS(selection.structure, (s) -> s.object.equals(childStructure.object));
            current = addPathSteps(current, parentInSelection, childInSelection);
            current = addListIfNeeded(current, childStructure);

            addChildTransformation(current, childStructure);
        });
    }

    private static class TransformationContext {
        public final Stack<ResultNode> inputs = new Stack<>();
        public final Stack<ResultNode> outputs = new Stack<>();
        public final Stack<NodeBuilder> builders = new Stack<>();

        public TransformationContext(ResultNode input) {
            inputs.push(input);
        }

        public ResultNode getOutput() {
            return outputs.pop();
        }
    }

    /**
     * This class represents one basic step in transforming a query result (ResultNode) to another one (with different QueryStructure).
     * It can be either a traverse (to a parent, a map or a list), a creation of a leaf, a creation of a map or a list, or a write to a map or a list.
     * The steps usually have children which are also traversed. Some steps spawn multiple new "branches" which are traversed one by one. Therefore, the whole transformation can be obtained by simply appliing the first step to the root node.
     * 
     * This system is designed to transform the data as fast as possible. We only have to create the steps once and then we can apply them to any amount of data.
     */
    private static abstract class TransformationStep {
        private final List<TransformationStep> children = new ArrayList<>();

        /**
         * Adds a child to this step. Then returns the child.
         * @param child A child step
         * @return The child step
         */
        public TransformationStep addChild(TransformationStep child) {
            children.add(child);

            return child;
        }

        public abstract void apply(TransformationContext context);

        protected void applyChildren(TransformationContext context) {
            children.forEach(child -> child.apply(context));
        }

        @Override
        public String toString() {
            final var builder = new LineStringBuilder(0, "  ");
            print(builder);
            return builder.toString();
        }

        abstract void print(LineStringBuilder builder);

        protected void printChildren(LineStringBuilder builder) {
            if (children.isEmpty()) {
                builder.nextLine();
                return;
            }

            if (children.size() == 1) {
                builder.nextLine().append("    ");
                children.get(0).print(builder);
                return;
            }

            builder.append(":").down().nextLine();
            children.forEach(child -> {
                builder.append("--- ");
                child.print(builder);
                builder.nextLine();
            });
            builder.up();
        }
    }

    private static class TransformationRoot extends TransformationStep {
        @Override
        public void apply(TransformationContext context) {
            applyChildren(context);
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("root");
            printChildren(builder);
        }
    }

    private static class TraverseParent extends TransformationStep {
        @Override
        public void apply(TransformationContext context) {
            final var lastContext = context.inputs.pop();
            applyChildren(context);
            context.inputs.push(lastContext);
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("T up");
            printChildren(builder);
        }
    }

    private static class TraverseMap extends TransformationStep {
        private final String key;

        public TraverseMap(String key) {
            this.key = key;
        }

        @Override
        public void apply(TransformationContext context) {
            final var currentMap = (ResultMap) context.inputs.peek();
            context.inputs.push(currentMap.children().get(key));
            applyChildren(context);
            context.inputs.pop();
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("T map (").append(key).append(")");
            printChildren(builder);
        }
    }

    private static class TraverseList extends TransformationStep {
        @Override
        public void apply(TransformationContext context) {
            final var currentList = (ResultList) context.inputs.peek();
            currentList.children().forEach(childContext -> {
                context.inputs.push(childContext);
                applyChildren(context);
                context.inputs.pop();
            });
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("T list");
            printChildren(builder);
        }
    }

    private static class CreateLeaf extends TransformationStep {
        @Override
        public void apply(TransformationContext context) {
            final var inputLeaf = (ResultLeaf) context.inputs.peek();
            final var outputLeaf = new ResultLeaf(inputLeaf.value);
            context.outputs.push(outputLeaf);
            // applyChildren(context);
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("C leaf");
            printChildren(builder);
        }
    }

    // The expected structure is this:
    // CreateMap -> TraverseMap with k1 -> WriteToMap with k1 -> ... possible other traverses ... -> ... chilren that creates the new map items
    //           -> TraverseMap with k2 -> WriteToMap with k2 -> ... possible other traverses ... -> ... chilren that creates the new map items
    //           -> ... the same with other keys ...
    private static class CreateMap extends TransformationStep {
        @Override
        public void apply(TransformationContext context) {
            final var builder = new ResultMap.Builder();
            context.builders.push(builder);
            applyChildren(context);
            context.builders.pop();
            context.outputs.push(builder.build());
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("C map");
            printChildren(builder);
        }
    }

    private static class WriteToMap extends TransformationStep {
        private final String key;

        public WriteToMap(String key) {
            this.key = key;
        }

        @Override
        public void apply(TransformationContext context) {
            applyChildren(context);
            final ResultNode outputNode = context.outputs.pop();
            final var builder = (ResultMap.Builder) context.builders.peek();
            builder.put(key, outputNode);
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("W map (").append(key).append(")");
            printChildren(builder);
        }
    }

    // The expected structure is this:
    // CreateList -> TraverseList -> ... possible other traverses ... -> WriteToList -> ... chilren that creates the new list items.
    private static class CreateList<T extends ResultNode> extends TransformationStep {
        @Override
        public void apply(TransformationContext context) {
            final var builder = new ResultList.Builder<T>();
            context.builders.push(builder);
            applyChildren(context);
            context.builders.pop();
            context.outputs.push(builder.build());
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("C list");
            printChildren(builder);
        }
    }

    private static class WriteToList<T extends ResultNode> extends TransformationStep {
        @Override
        public void apply(TransformationContext context) {
            applyChildren(context);
            final var outputNode = (T) context.outputs.pop();
            final var builder = (ResultList.Builder<T>) context.builders.peek();
            builder.add(outputNode);
        }

        @Override
        protected void print(LineStringBuilder builder) {
            builder.append("W list");
            printChildren(builder);
        }
    }

}