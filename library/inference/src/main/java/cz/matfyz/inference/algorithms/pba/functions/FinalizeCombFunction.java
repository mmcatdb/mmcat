package cz.matfyz.inference.algorithms.pba.functions;

import cz.matfyz.core.rsd.RecordSchemaDescription;
import org.apache.spark.api.java.function.Function2;

public class FinalizeCombFunction implements Function2<RecordSchemaDescription, RecordSchemaDescription, RecordSchemaDescription> {

    @Override
    public RecordSchemaDescription call(RecordSchemaDescription schemaRoot, RecordSchemaDescription property) throws Exception {
        String hierarchicalName = property.getName();
        String[] hierarchies = hierarchicalName.split("/");
        property.setName(hierarchies[hierarchies.length - 1]);
        traverseSchema(property, hierarchies, schemaRoot, 0);

        return schemaRoot;
    }

    private void traverseSchema(RecordSchemaDescription property, String[] hierarchies, RecordSchemaDescription schema, int depth) {
        for (RecordSchemaDescription subtree : schema.getChildren()) {
            if (subtree.getName().equals(hierarchies[depth])) {    //find the subtree that is of the same name as the current node name
                createNodeInSchema(property, hierarchies, subtree, depth);        //add the node to the subtree and return
                return;
            }
        }
        RecordSchemaDescription subtree = new RecordSchemaDescription();
        subtree.setName(hierarchies[depth]); //no subtree has been found, create one with the current node name
        createNodeInSchema(property, hierarchies, subtree, depth);        //add the node to the subtree

        schema.getChildren().add(subtree);
    }

    private void createNodeInSchema(RecordSchemaDescription property, String[] hierarchies, RecordSchemaDescription tree, int index) {
        if (hierarchies.length > index + 1) {        //if the current node is not the last node in hierarchicalName
            traverseSchema(property, hierarchies, tree, index + 1);         //proceed with the next node name
        } else {
            tree.setUnique(property.getUnique());
            tree.setShareTotal(property.getShareTotal());
            tree.setShareFirst(property.getShareFirst());
            tree.setUnique(property.getId());
            tree.setTypes(tree.getTypes() | property.getTypes());
            tree.setModels(property.getModels());
//            tree.setRegExp(property.getSchema().getRegExp());
//            tree.setRef(property.getSchema().getRef());
        }
    }

}
