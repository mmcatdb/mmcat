package cz.matfyz.inference.schemaconversion.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.*;
import cz.matfyz.core.schema.SchemaCategory;

public class MappingCreator {
    public Key rootKey;
    public AccessTreeNode root;
    private MappingBuilder builder = new MappingBuilder();
    
    public MappingCreator(Key rootKey, AccessTreeNode root) {
        this.rootKey = rootKey;
        this.root = root;
    }

   public Mapping createMapping(SchemaCategory sc, String kindName) {
       System.out.println("access tree: ");
       root.printTree(" ");
       ComplexProperty accessPath = buildComplexPropertyFromNode(root);
       System.out.println("This is root Key: " + rootKey);
       return Mapping.create(sc, rootKey, kindName, accessPath);
   }

   public ComplexProperty buildComplexPropertyFromNode(AccessTreeNode node) {
       List<AccessPath> subpaths = new ArrayList<>();

       for (AccessTreeNode child : node.getChildren()) {
           if (child.getState() == AccessTreeNode.State.Simple) {
               subpaths.add(builder.simple(child.getName(), child.getSig()));
           } else {
               subpaths.add(buildComplexPropertyFromNode(child));
           }
       }

       if (node.getState() == AccessTreeNode.State.Root) {
           System.out.println("adding root to mapping");
           return builder.root(subpaths.toArray(new AccessPath[0]));
       } else {
           System.out.println("creating complex property for: " + node.getName());
           return builder.complex(node.getName(), node.getSig(), subpaths.toArray(new AccessPath[0]));
       }
   }
 
}



