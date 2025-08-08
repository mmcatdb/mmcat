package cz.matfyz.core.rsd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.Serializable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class RecordSchemaDescription implements Serializable, Comparable<RecordSchemaDescription> {

    public static final String ROOT_SYMBOL = "_";

    private String name;

    private int unique;    // TODO: rozepsat na konstanty vestaveneho datoveho typu char, podobne jako typy a modely

    private int shareTotal;    // TODO: rozepsat na dve property, int total a int first

    private int shareFirst;

    private int id;        // TODO: rozepsat na konstanty vestaveneho datoveho typu char, podobne jako typy a modely

    private /*Set<Type>*/ int types;

    private /*Set<Model>*/ int models;

    private /*List*/ ObjectArrayList<RecordSchemaDescription> children;    // TODO: pouzit knihovnu https://trove4j.sourceforge.net/javadocs/gnu/trove/list/linked/TLinkedList.html nebo podobne efektivni a vhodnou - mene vytvorenych objektu, pametove uspornejsi a ve vysledku rychlejsi


    public RecordSchemaDescription() {
        this("", Char.UNKNOWN, 0, 0/*new Share()*/, Char.UNKNOWN, /*new TreeSet<>(), new TreeSet<>(),*/ new /*ArrayList*/ ObjectArrayList<>());
    }

    public RecordSchemaDescription(
            String name,
            int unique,
            int shareTotal, int shareFirst, //Share share,
            int id,
            //            Set<Type> types,
            //            Set<Model> models,
            ObjectArrayList<RecordSchemaDescription> children
            ) {
        this.name = name;
        this.unique = unique;
//        this.share = share;
        this.shareTotal = shareTotal;
        this.shareFirst = shareFirst;
        this.id = id;
        this.types = 0;
        this.models = 0;
        this.children = children;
    }
    // constructor for cloning
    public RecordSchemaDescription(RecordSchemaDescription other) {
        this.name = other.name;
        this.unique = other.unique;
        this.shareTotal = other.shareTotal;
        this.shareFirst = other.shareFirst;
        this.id = other.id;
        this.types = other.types;
        this.models = other.models;
        this.children = new ObjectArrayList<>();
        for (RecordSchemaDescription child : other.children) {
            this.children.add(new RecordSchemaDescription(child));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnique() {
        return unique;
    }

    public void setUnique(int unique) {
        this.unique = unique;
    }

    public int getShareTotal() {
        return shareTotal;
    }

    public void setShareTotal(int shareTotal) {
        this.shareTotal = shareTotal;
    }

    public int getShareFirst() {
        return shareFirst;
    }

    public void setShareFirst(int shareFirst) {
        this.shareFirst = shareFirst;
    }

//    public Share getShare() {
//        return share;
//    }
//
//    public void setShare(Share share) {
//        this.share = share;
//    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public /*Set<Type>*/ int getTypes() {
        return types;
    }

    public void setTypes(/*Set<Type>*/int types) {
        this.types = types;
    }

    public /*Set<Model>*/ int getModels() {
        return models;
    }

    public void setModels(/*Set<Model>*/int models) {
        this.models = models;
    }

    public /*List*/ ObjectArrayList<RecordSchemaDescription> getChildren() {
        return children;
    }

    public void setChildren(/*List*/ObjectArrayList<RecordSchemaDescription> children) {
        this.children = children;
    }

    // Utility methods
    public boolean hasParentWithChildName(String childName) {
        for (RecordSchemaDescription child : this.children) {
            if (child.getName().equals(childName))
                return true;
            child.hasParentWithChildName(childName);
        }
        return false;
    }

    public boolean addChildrenIfNameMatches(RecordSchemaDescription rsd) {
        if (this.name.equals(rsd.getName())) {
            if (isReferencingRSD(this)) {
                ObjectArrayList<RecordSchemaDescription> newChildren = new ObjectArrayList<>();
                this.setChildren(newChildren);
            }
            for (RecordSchemaDescription child : rsd.getChildren()) {
                addChildren(child);
            }
            return true;
        }
        for (RecordSchemaDescription oldChild : this.children) {
            if (oldChild.addChildrenIfNameMatches(rsd)) {
                return true;
            }
        }
        return false;
    }

    public void addChildren(RecordSchemaDescription child) {
        ObjectArrayList<RecordSchemaDescription> newChildren = new ObjectArrayList<>();
        for (RecordSchemaDescription oldChild : this.children) {
            newChildren.add(oldChild);
        }
        newChildren.add(child);
        this.setChildren(newChildren);
    }

    private boolean isReferencingRSD(RecordSchemaDescription rsd) {
        for (RecordSchemaDescription child : rsd.getChildren()) {
            if (!child.getName().equals(ROOT_SYMBOL)) {
                return false;
            }
        }
        return true;
    }

    public boolean removeChildByName(String name) {
        for (RecordSchemaDescription child : children) {
            if (child.getName().equals(name)) {
                children.remove(child);
                return true;
            } else if (child.removeChildByName(name))
                return true;
        }
        return false;
    }

    // end of utility methods

    @Override public int compareTo(RecordSchemaDescription o) {
        int comparedNames = name.compareTo(o.name);
        if (comparedNames != 0) {
            return comparedNames;
        }
        return 0;
        //return types - o.types;
    }

    public String _toString() {
        return new StringBuilder()
            .append("RecordSchemaDescription{")
            .append("name=").append(name)
            .append(", unique=").append(unique)
            // .append(", share=").append(share)
            .append(", shareTotal=").append(shareTotal)
            .append(", shareFirst=").append(shareFirst)
            .append(", id=").append(id)
            .append(", types=").append(types)
            .append(", models=").append(models)
            .append(", children=").append(children)
            .append('}')
            .toString();
    }

    @Override public String toString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(SerializationFeature.INDENT_OUTPUT);    // pretty print disabled

            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return _toString(); // super.toString(); // Fallback to the default toString() if an exception occurs
        }
    }
}
