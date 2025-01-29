package cz.matfyz.querying.resolver.queryresult;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.LeafResult;
import cz.matfyz.core.querying.MapResult;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.core.utils.GraphUtils.TreePath;
import cz.matfyz.querying.resolver.queryresult.TformStep.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a transformation that transforms a query result corresponding to the input structure to a query result corresponding to the output structure.
 */
public class ResultStructureTformer {

    public static TformRoot run(ResultStructure inputStructure, TformingResultStructure outputStructure) {
        return new ResultStructureTformer(inputStructure, outputStructure).run();
    }

    private final ResultStructure inputStructure;
    private final TformingResultStructure outputStructure;

    private ResultStructureTformer(ResultStructure inputStructure, TformingResultStructure outputStructure) {
        this.inputStructure = inputStructure;
        this.outputStructure = outputStructure;
    }

    private TformRoot run() {
        final ResultStructure rootInSelection = GraphUtils.findDFS(inputStructure, s -> outputStructure.inputName.equals(s.name));

        // TODO proper exception
        if (rootInSelection == null)
            throw new UnsupportedOperationException("Root not found in the selection structure.\n" + inputStructure + "\n" + outputStructure.toResultStructure());

        final var root = new TformRoot();
        TformStep current = root;

        current = current
            .addChild(new CreateList<MapResult>())
            .addChild(new TraverseList());

        final var path = GraphUtils.findPath(inputStructure, rootInSelection);
        current = addPathSteps(current, path);
        current = current.addChild(new WriteToList<MapResult>());

        addChildTforms(current, outputStructure);

        return root;
    }

    /**
     * Both source and target have to be in the selection structure.
     */
    public static TformStep addPathSteps(TformStep current, TreePath<ResultStructure> path) {
        // We ignore the last element because we don't want to travel from it.
        for (int i = 0; i < path.sourceToRoot().size() - 1; i++) {
            current = current.addChild(new TraverseParent());
            if (path.sourceToRoot().get(i).isArray)
                current = current.addChild(new TraverseParent());
        }

        // For each element, we travel to it. Therefore we skip the root element - we are already there, no need to travel.
        for (int i = 1; i < path.rootToTarget().size(); i++) {
            final var structure = path.rootToTarget().get(i);
            current = current.addChild(new TraverseMap(structure.name));
            if (structure.isArray)
                current = current.addChild(new TraverseList());
        }

        return current;
    }

    public static boolean isPathArray(TreePath<ResultStructure> path) {
        for (int i = 1; i < path.rootToTarget().size(); i++)
            if (path.rootToTarget().get(i).isArray)
                return true;

        return false;
    }

    public static Signature computePathSignature(TreePath<ResultStructure> path) {
        final List<Signature> outputList = new ArrayList<>();
        final List<ResultStructure> toRoot = path.sourceToRoot();
        // We skip the last element (i.e., the root) because we are not traveling above it. Then we take the dual because we are traveling up.
        for (int i = 0; i < toRoot.size() - 1; i++)
            outputList.add(toRoot.get(i).signatureFromParent.dual());

        // For the same reason, we skip the first element here.
        final List<ResultStructure> toTarget = path.rootToTarget();
        for (int i = 1; i < toTarget.size(); i++)
            outputList.add(toTarget.get(i).signatureFromParent);

        return Signature.concatenate(outputList);
    }

    // Let A, B, ... be a ResultStructure, the [] symbol mens it has isArray = true and the -> symbol means its children. Then:
    //  - A[] -> B? : CreateList<MapResult>, then CreateMap
    //  - A   -> B? : CreateMap
    //  - A[]       : CreateList<LeafResult>, then CreateLeaf
    //  - A         : CreateLeaf
    // The list of lists is not supported.
    private void addChildTforms(TformStep current, TformingResultStructure childStructure) {
        if (childStructure.children.isEmpty())
            current.addChild(new CreateLeaf());
        else
            createMap(current, childStructure);
    }

    private void createMap(TformStep parentStep, TformingResultStructure structure) {
        final var mapStep = parentStep.addChild(new CreateMap());
        structure.children.forEach(childStructure -> {
            var current = mapStep.addChild(new WriteToMap(childStructure.outputName));

            final var parentInSelection = GraphUtils.findDFS(inputStructure, s -> s.name.equals(structure.inputName));
            final var childInSelection = GraphUtils.findDFS(inputStructure, s -> s.name.equals(childStructure.inputName));
            if (childInSelection == null)
                throw new UnsupportedOperationException("Term \"" + childStructure.inputName + "\" not found in the selection structure.");

            final var path = GraphUtils.findPath(parentInSelection, childInSelection);
            childStructure.setPathInfo(isPathArray(path), computePathSignature(path));

            current = createListIfNeeded(current, childStructure);
            current = addPathSteps(current, path);
            current = writeToListIfNeeded(current, childStructure);

            addChildTforms(current, childStructure);
        });
    }

    private static TformStep createListIfNeeded(TformStep parentStep, TformingResultStructure structure) {
        if (!structure.isArray())
            return parentStep;

        return structure.children.isEmpty()
            ? parentStep.addChild(new CreateList<LeafResult>())
            : parentStep.addChild(new CreateList<MapResult>());
    }

    private static TformStep writeToListIfNeeded(TformStep parentStep, TformingResultStructure structure) {
        if (!structure.isArray())
            return parentStep;

        return structure.children.isEmpty()
            ? parentStep.addChild(new WriteToList<LeafResult>())
            : parentStep.addChild(new WriteToList<MapResult>());
    }

}
