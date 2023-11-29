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
import cz.matfyz.core.utils.GraphUtils.TreePath;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.exception.ProjectionException;
import cz.matfyz.querying.parsing.Aggregation;
import cz.matfyz.querying.parsing.SelectClause;
import cz.matfyz.querying.parsing.SelectTriple;
import cz.matfyz.querying.parsing.Variable;
import cz.matfyz.querying.parsing.ParserNode.Term;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class QueryProjector {

    public static QueryResult run(QueryContext context, SelectClause selectClause, QueryResult selection) {
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
    
    private QueryResult run() {
        final TransformingQueryStructure projectionStructure = computeProjectionStructure();
        final TransformationRoot transformation = QueryStructureTransformer.run(selection.structure, projectionStructure);
        final var transformationContext = new TransformationContext(selection.data);
        
        transformation.apply(transformationContext);

        final ResultList data = (ResultList) transformationContext.getOutput();

        return new QueryResult(data, projectionStructure.toQueryStructure());
    }
    
    private TransformingQueryStructure computeProjectionStructure() {
        final var rootVariable = findRootVariable();
        final var projectionStructure = new TransformingQueryStructure(rootVariable.getIdentifier(), rootVariable.getIdentifier());
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

    private void addChildrenToStructure(Variable parentVariable, TransformingQueryStructure parentStructure) {
        selectClause.triples.stream().filter(t -> t.subject.equals(parentVariable)).forEach(t -> {
            // We don't know (yet) if the structure is supposed to be an array. We fill figure it out later during the transformation.
            // Like we can find out now, but that would require doing the whole tree search again.
            final var childStructure = new TransformingQueryStructure(t.object.getIdentifier(), t.name);
            parentStructure.children.add(childStructure);
            if (t.object instanceof Variable childVariable)
                addChildrenToStructure(childVariable, childStructure);
        });
    }

    public static class TransformingQueryStructure {

        public final String inputName;
        public final String outputName;
        public final List<TransformingQueryStructure> children = new ArrayList<>();
        /** This is supposed to be determined later during the transformation. */
        public boolean isArray = false;

        public TransformingQueryStructure(String inputName, String outputName) {
            this.inputName = inputName;
            this.outputName = outputName;
        }

        public QueryStructure toQueryStructure() {
            final var output = new QueryStructure(outputName, isArray);
            children.forEach(child -> output.addChild(child.toQueryStructure()));

            return output;
        }
    }

    public static class QueryStructureTransformer {

        public static TransformationRoot run(QueryStructure inputStructure, TransformingQueryStructure outputStructure) {
            return new QueryStructureTransformer(inputStructure, outputStructure).run();
        }

        private final QueryStructure inputStructure;
        private final TransformingQueryStructure outputStructure;

        private QueryStructureTransformer(QueryStructure inputStructure, TransformingQueryStructure outputStructure) {
            this.inputStructure = inputStructure;
            this.outputStructure = outputStructure;
        }

        private TransformationRoot run() {
            final QueryStructure rootInSelection = GraphUtils.findDFS(inputStructure, s -> outputStructure.inputName.equals(s.name));

            // TODO proper exception
            if (rootInSelection == null)
                throw new UnsupportedOperationException("Root not found in the selection structure.\n" + inputStructure + "\n" + outputStructure.toQueryStructure());

            final var root = new TransformationRoot();
            TransformationStep current = root;
            
            current = current
                .addChild(new CreateList<ResultMap>())
                .addChild(new TraverseList());
                
            final var path = GraphUtils.findPath(inputStructure, rootInSelection);
            current = addPathSteps(current, path);
            current = current.addChild(new WriteToList<ResultMap>());

            addChildTransformation(current, outputStructure);

            return root;
        }

        /**
         * Both source and target have to be in the selection structure.
         */
        private TransformationStep addPathSteps(TransformationStep current, TreePath<QueryStructure> path) {
            // We ignore the last element because we don't want to travel from it.
            for (int i = 0; i < path.sourceToRoot().size() - 1; i++) {
                current = current.addChild(new TraverseParent());
                if (path.sourceToRoot().get(i).isArray)
                    current = current.addChild(new TraverseParent());
            }

            // For each element, we travel to it. Therefore we skip the root element - we are already there, no need to travel.
            // for (int i = 1; i < path.rootToTarget().size() - 1; i++) {
            for (int i = 1; i < path.rootToTarget().size(); i++) {
                final var structure = path.rootToTarget().get(i);
                current = current.addChild(new TraverseMap(structure.name));
                if (structure.isArray)
                    current = current.addChild(new TraverseList());
            }

            return current;
        }

        private boolean isPathArray(TreePath<QueryStructure> path) {
            for (int i = 1; i < path.rootToTarget().size(); i++)
                if (path.rootToTarget().get(i).isArray)
                    return true;

            return false;
        }

        // Let A, B, ... be a QueryStructure, the [] symbol mens it has isArray = true and the -> symbol means its children. Then:
        //  - A[] -> B? : CreateList<ResultMap>, then CreateMap
        //  - A   -> B? : CreateMap
        //  - A[]       : CreateList<ResultLeaf>, then CreateLeaf
        //  - A         : CreateLeaf
        // The list of lists is not supported.
        private void addChildTransformation(TransformationStep current, TransformingQueryStructure childStructure) {
            if (childStructure.children.isEmpty())
                current.addChild(new CreateLeaf());
            else
                createMap(current, childStructure);
        }

        private TransformationStep createListIfNeeded(TransformationStep parentStep, TransformingQueryStructure structure) {
            if (!structure.isArray)
                return parentStep;

            return structure.children.isEmpty()
                ? parentStep.addChild(new CreateList<ResultLeaf>())
                : parentStep.addChild(new CreateList<ResultMap>());
        }

        private TransformationStep writeToListIfNeeded(TransformationStep parentStep, TransformingQueryStructure structure) {
            if (!structure.isArray)
                return parentStep;

            return structure.children.isEmpty()
                ? parentStep.addChild(new WriteToList<ResultLeaf>())
                : parentStep.addChild(new WriteToList<ResultMap>());
        }

        private void createMap(TransformationStep parentStep, TransformingQueryStructure structure) {
            final var mapStep = parentStep.addChild(new CreateMap());
            structure.children.forEach(childStructure -> {
                var current = mapStep.addChild(new WriteToMap(childStructure.outputName));

                final var parentInSelection = GraphUtils.findDFS(inputStructure, s -> s.name.equals(structure.inputName));
                final var childInSelection = GraphUtils.findDFS(inputStructure, s -> s.name.equals(childStructure.inputName));
                if (childInSelection == null)
                    throw new UnsupportedOperationException("Term " + childStructure.inputName + " not found in the selection structure.");

                final var path = GraphUtils.findPath(parentInSelection, childInSelection);
                
                childStructure.isArray = isPathArray(path);
                current = createListIfNeeded(current, childStructure);
                current = addPathSteps(current, path);
                current = writeToListIfNeeded(current, childStructure);

                addChildTransformation(current, childStructure);
            });
        }

    }

    public static class TransformationContext {
        public final Deque<ResultNode> inputs = new ArrayDeque<>();
        public final Deque<ResultNode> outputs = new ArrayDeque<>();
        public final Deque<NodeBuilder> builders = new ArrayDeque<>();

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
    public static abstract class TransformationStep {
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
            final var builder = new LineStringBuilder(0, "    ");
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
            builder.remove();
            builder.up();
        }
    }

    public static class TransformationRoot extends TransformationStep {
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
            builder.append("T.up");
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
            builder.append("T.map(").append(key).append(")");
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
            builder.append("T.list");
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
            builder.append("C.leaf");
            printChildren(builder);
        }
    }

    // The expected structure is this:
    // CreateMap -> WriteToMap with k1 -> TraverseMap with k1 -> ... possible other traverses ... -> ... children that creates the new map items
    //           -> WriteToMap with k2 -> TraverseMap with k2 -> ... possible other traverses ... -> ... children that creates the new map items
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
            builder.append("C.map");
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
            builder.append("W.map(").append(key).append(")");
            printChildren(builder);
        }
    }

    // The expected structure is this:
    // CreateList -> TraverseList -> ... possible other traverses ... -> WriteToList -> ... children that creates the new list items.
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
            builder.append("C.list");
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
            builder.append("W.list");
            printChildren(builder);
        }
    }

}