package cz.matfyz.tests.example.common;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.instance.MappingRow;
import cz.matfyz.core.instance.SuperIdWithValues;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SimpleBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author jachymb.bartik
 */
public class InstanceBuilder {

    private final InstanceCategory instance;

    public InstanceBuilder(SchemaCategory schema) {
        this.instance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();
    }

    public InstanceCategory build() {
        return this.instance;
    }

    private final SuperIdWithValues.Builder superIdBuilder = new SuperIdWithValues.Builder();

    public InstanceBuilder value(Signature signature, String value) {
        superIdBuilder.add(signature, value);

        return this;
    }

    public DomainRow object(SimpleBuilder.Object object) {
        return object(object.key());
    }

    public DomainRow object(Key key) {
        final var instanceObject = instance.getObject(key);
        final SuperIdWithValues superId = superIdBuilder.build();

        var row = instanceObject.getRow(superId);
        if (row == null)
            row = instanceObject.getOrCreateRow(superId);

        createdRows.computeIfAbsent(key, k -> new ArrayList<>()).add(row);

        return row;
    }


    public DomainRow valueObject(String value, SimpleBuilder.Object object) {
        return valueObject(value, object.key());
    }

    public DomainRow valueObject(String value, Key key) {
        return value(Signature.createEmpty(), value).object(key);
    }

    public MappingRow morphism(Signature signature, DomainRow domainRow, DomainRow codomainRow) {
        var row = new MappingRow(domainRow, codomainRow);
        instance.getMorphism(signature).addMapping(row);

        return row;
    }

    public void morphism(Signature signature) {
        instance.getMorphism(signature);
    }

    private Map<Key, List<DomainRow>> createdRows = new TreeMap<>();

    public List<DomainRow> getRows(Key key) {
        return createdRows.get(key);
    }

    public DomainRow getRow(Key key, int index) {
        return createdRows.get(key).get(index);
    }

    public interface InstanceAdder {
        void add(InstanceBuilder builder);
    }

}
