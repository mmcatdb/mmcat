package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.exception.InvalidNameException;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor.PropertyValue;
import cz.matfyz.core.exception.OtherException;

import java.util.ArrayList;
import java.util.List;

import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RiakDMLWrapper implements AbstractDMLWrapper {

    private final RiakProvider riakProvider;

    public RiakDMLWrapper(RiakProvider riakProvider) {
        this.riakProvider = riakProvider;
    }

    @Override public void clear() {
        kindName = null;
        propertyValues.clear();
    }

    private String kindName = null;
    private final List<PropertyValue> propertyValues = new ArrayList<>();

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public void append(String name, @Nullable Object value) {
        String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(name, stringValue));
    }

    @Override public RiakCommandStatement createDMLStatement() {
        if (kindName == null)
            throw InvalidNameException.kind(null);

        String bucketType = riakProvider.settings.bucketType();
        String propertiesJson;
        final var constructor = new JsonDMLConstructor();

        try {
            for (var propertyValue : propertyValues)
                constructor.addProperty(propertyValue);

            propertiesJson = constructor.toString();
        }
        catch (Exception e) {
            throw new OtherException(e);
        }

        // random key is assigned to a new object

        RiakObject riakObject = new RiakObject()
            .setContentType("application/json")
            .setValue(BinaryValue.create(propertiesJson));
        StoreValue command = new StoreValue.Builder(riakObject)
            .withNamespace(new Namespace(bucketType, kindName))
            .build();

        String shellCommand = String.format(
            """
                curl -X PUT
                    -H 'Content-Type: application/json'
                    -d '%s'
                    %s/types/%s/buckets/%s/keys""",
            propertiesJson,
            riakProvider.settings.createHttpConnectionString(),
            bucketType,
            kindName);

        return new RiakCommandStatement(shellCommand, command);
    }
}
