package cz.matfyz.inference.schemaconversion.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.schema.SchemaCategory;

public class MappingCreator {
    public Key rootKey;
    public AccessTreeNode root;
    
    public MappingCreator(Key rootKey, AccessTreeNode root) {
        this.rootKey = rootKey;
        this.root = root;
    }
    /**
    * Method for creating Mapping for the SchemaCategory
    * */
   public Mapping createMapping(SchemaCategory sc, String kindName) {
       ComplexProperty accessPath = buildComplexPropertyFromNode(root);
       //System.out.println(accessPath);
       //AccessPath ap = accessPath.tryGetSubpathForObject(rootKey, sc)
       return Mapping.create(sc, rootKey, kindName, accessPath);
   }

   /**
    * Method for creating root Complex property which later serves as the access path for the SchemaCat
    * It does so by recursively traversing tree where each node corresponds to a Simple/Complex property
    * @param node
    * @return
    */
   public ComplexProperty buildComplexPropertyFromNode(AccessTreeNode node) {
       List<AccessPath> subpaths = new ArrayList<>();

       for (AccessTreeNode child : node.getChildren()) {
           if (child.getState() == AccessTreeNode.State.Simple) {
               //System.out.println("indeed i am creating simpleproperties");
               //System.out.println("child name: "+child.getName());
               subpaths.add(new SimpleProperty(new StaticName(child.getName()), child.getSig()));
           } else {
               subpaths.add(buildComplexPropertyFromNode(child));
           }
       }
       if (node.getState() == AccessTreeNode.State.Simple) {
           System.out.println("creating 1: " + node.getName());
           return new ComplexProperty(new StaticName(node.getName()), node.getSig(), subpaths);
       } else {
           //System.out.println("indeed i am creating complex2");
           System.out.println("creating 2: " + node.getName());
           AccessPath[] subpathsArr = subpaths.toArray(new AccessPath[0]);
           //create auxiliary for root
           if (node.getName().equals("yelpbusinesssample") ) {
               System.out.println("complex prop for root made auxiliary");
               return new ComplexProperty(StaticName.createAnonymous(), Signature.createEmpty(), new ArrayList<>(Arrays.asList(subpathsArr)));  
           }
           else {
           //AccessPath[] subpathsArr = subpaths.toArray(new AccessPath[0]);
           return new ComplexProperty(new StaticName(node.getName()), node.getSig(), new ArrayList<>(Arrays.asList(subpathsArr))); }
       }
   }


}
