/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.contos
 */
public class RelationshipMorphism implements CategoricalMorphism {

    @Override
    public Object getValue(EntityObject.EntityValue key) {
        for (var mapping : mappings) {
            if (mapping.key.equals(key)) {
                return mapping.value;
            }
        }
        return null;
    }

    public void addMapping(Object sid, Object embeddedSID) {
//        System.out.println("RelationshipMorphism -> addMapping() ... namisto sid posilej primo entityValue identifikatoru, at je to provazane, nebo lepe objekt identifikatoru");
        List<List<Object>> set = new ArrayList<>();
        List<Object> identifier = new ArrayList<>();
        identifier.add(sid);
        set.add(identifier);
        EntityObject.EntityValue entityValue = new EntityObject.EntityValue(set);
        mappings.add(new KeyReferencePair(entityValue, embeddedSID));
    }

    private static final class KeyReferencePair implements Comparable<KeyReferencePair> {

        private final EntityObject.EntityValue key;
        private final Object value;

        public KeyReferencePair(EntityObject.EntityValue key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(KeyReferencePair other) {
            int keyComparison = key.compareTo(other.key);

            // ERROR: Umoznovat pouze comparable objekty vkladat? protoze object neumoznuje compare to!
            // tohle umozni duplicity!
            return keyComparison != 0 ? keyComparison : -1;// value.compareTo(other.value);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            builder.append(key);
            builder.append(",");
            builder.append(value);
            builder.append(")");
            return builder.toString();
        }
    }

    private final String morphismName;
    private final CategoricalObject domain;
    private final CategoricalObject codomain;
    private final Set<RelationshipMorphism.KeyReferencePair> mappings = new TreeSet<>();

    public RelationshipMorphism(String morphismName, CategoricalObject domain, CategoricalObject codomain) {
        this.morphismName = morphismName;
        this.domain = domain;
        this.codomain = codomain;
    }

    @Override
    public void add(EntityObject.EntityValue key, Object value) {
        mappings.add(new RelationshipMorphism.KeyReferencePair(key, value));
    }

    @Override
    public String getDomain() {
        return domain.getName();
    }

    @Override
    public String getCodomain() {
        return codomain.getName();
    }

    @Override
    public String getName() {
        return morphismName;
    }

    @Override
    public int compareTo(CategoricalMorphism other) {
        return morphismName.compareTo(other.getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(morphismName);
        builder.append(":\t");

        builder.append("{");

        boolean firstValue = true;
        for (var value : mappings) {
            if (firstValue) {
                firstValue = !firstValue;
            } else {
                builder.append(",");
            }
            builder.append(value);
        }

        builder.append("}");
        return builder.toString();
    }

}
