package cz.matfyz.core.rsd.helpers;

import cz.matfyz.core.rsd.ProcessedProperty;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RecordSchemaDescription;

import java.util.List;
import java.util.Set;

public enum BuildPropertyTree {
    INSTANCE;

    public ProcessedProperty process(List<ProcessedProperty> properties) {
        ProcessedProperty tree = buildTree(properties);
        setFinalHeuristics(tree);
        return tree;
    }

    private void setFinalHeuristics(ProcessedProperty tree) {
        for (ProcessedProperty property : tree.getChildren()) {
            PropertyHeuristics heuristics = property.getHeuristics();
            // uncoment to repair legacy
            // heuristics.setParentCount(tree.getHeuristics().getCount());
            // heuristics.setRequired(heuristics.getCount() == heuristics.getParentCount());
            if (heuristics.getMin() instanceof Number && heuristics.getMax() instanceof Number) {
                double min = ((Number) heuristics.getMin()).doubleValue();
                double max = ((Number) heuristics.getMax()).doubleValue();
                if (min % 1 == 0 && max % 1 == 0) {    //test if the values are integers
                    if (max - min <= heuristics.getCount() - 1) {
                        heuristics.setSequential(true);
                    }
                }
            }
            Set<ProcessedProperty> children = property.getChildren();
            if (children.size() > 0) {
                setFinalHeuristics(property);
            }
        }
    }

    private ProcessedProperty buildTree(List<ProcessedProperty> processedProperties) {
        ProcessedProperty result = new ProcessedProperty("", new RecordSchemaDescription());

        for (ProcessedProperty property : processedProperties) {
            String hierarchicalName = property.getHierarchicalName();

            String[] nodes = hierarchicalName.split("/");
            addPropertyToTree(property, nodes, result, 0);
        }

        if (result.getChildren().isEmpty())
            return result;
        else {
            result = (ProcessedProperty) result.getChildren().toArray()[0];    //remove fake root element
        }
        return result;
    }

    private void addPropertyToTree(ProcessedProperty property, String[] nodes, ProcessedProperty tree, int index) {
        for (ProcessedProperty subtree : tree.getChildren()) {
            if (subtree.getHierarchicalName().equals(nodes[index])) {    //find the subtree that is of the same name as the current node name
                addNodeToTree(property, nodes, index, subtree);        //add the node to the subtree and return
                return;
            }
        }
        ProcessedProperty subtree = new ProcessedProperty(nodes[index], new RecordSchemaDescription());    //no subtree has been found, create one with the current node name
        addNodeToTree(property, nodes, index, subtree);        //add the node to the subtree
        tree.addChild(subtree);     //add subtree to the tree
        property.setParent(tree);
    }

    private void addNodeToTree(ProcessedProperty property, String[] nodes, int index, ProcessedProperty tree) {
        if (nodes.length > index + 1) {        //if the current node is not the last node in hierarchicalName
            addPropertyToTree(property, nodes, tree, index + 1);         //proceed with the next node name
        } else {
            tree.setHeuristics(property.getHeuristics());
        }
    }
}

