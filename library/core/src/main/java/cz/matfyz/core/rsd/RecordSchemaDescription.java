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

    private int id;        // TODO: rozepsat na konstanty vestaveneho datoveho typu char, podobne jako typy a modely

    private int shareTotal;

    private int shareFirst;

    private /*Set<Type>*/ int types;

    private ObjectArrayList<RecordSchemaDescription> children;    // TODO: pouzit knihovnu https://trove4j.sourceforge.net/javadocs/gnu/trove/list/linked/TLinkedList.html nebo podobne efektivni a vhodnou - mene vytvorenych objektu, pametove uspornejsi a ve vysledku rychlejsi


    public RecordSchemaDescription() {
        this("", Char.UNKNOWN, Char.UNKNOWN, 0, 0 /*new TreeSet<>(), new TreeSet<>(),*/);
    }

    public RecordSchemaDescription(
        String name,
        int unique,
        int id,
        int shareTotal,
        int shareFirst
        //          Set<Type> types,
    ) {
        this.name = name;
        this.unique = unique;
        this.id = id;
        this.shareTotal = shareTotal;
        this.shareFirst = shareFirst;
        this.types = 0;
        this.children = new ObjectArrayList<>();
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public /*Set<Type>*/ int getTypes() {
        return types;
    }

    public void setTypes(/*Set<Type>*/int types) {
        this.types = types;
    }

    public ObjectArrayList<RecordSchemaDescription> getChildren() {
        return children;
    }

    public void setChildren(ObjectArrayList<RecordSchemaDescription> children) {
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
                final var newChildren = new ObjectArrayList<RecordSchemaDescription>();
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
        final var newChildren = new ObjectArrayList<RecordSchemaDescription>();
        for (final var oldChild : this.children)
            newChildren.add(oldChild);

        newChildren.add(child);
        this.setChildren(newChildren);
    }

    private boolean isReferencingRSD(RecordSchemaDescription rsd) {
        for (final var child : rsd.getChildren()) {
            if (!child.getName().equals(ROOT_SYMBOL))
                return false;
        }
        return true;
    }

    public boolean removeChildByName(String name) {
        for (final var child : children) {
            if (child.getName().equals(name)) {
                children.remove(child);
                return true;
            }
            else if (child.removeChildByName(name)) {
                return true;
            }
        }
        return false;
    }

    // end of utility methods

    @Override public int compareTo(RecordSchemaDescription o) {
        return name.compareTo(o.name);
    }

    public String _toString() {
        return new StringBuilder()
            .append("RecordSchemaDescription{")
            .append("name=").append(name)
            .append(", unique=").append(unique)
            .append(", shareTotal=").append(shareTotal)
            .append(", shareFirst=").append(shareFirst)
            .append(", id=").append(id)
            .append(", types=").append(types)
            .append(", children=").append(children)
            .append('}')
            .toString();
    }

    private final static ObjectMapper mapper = new ObjectMapper();

    static {
        // Disable pretty print so that each RSD fits on a single line in logs.
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override public String toString() {
        try {
            return mapper.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            return _toString();
        }
    }
}
