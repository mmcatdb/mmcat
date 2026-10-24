package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.exception.InvalidNameException;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor.PropertyValue;
import cz.matfyz.core.exception.OtherException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RiakDMLWrapper implements AbstractDMLWrapper {

    private final RiakProvider provider;

    public RiakDMLWrapper(RiakProvider provider) {
        this.provider = provider;
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
        // We are using Riak Yokozuna Search with the default schema (_yz_default)
        // Solr uses the suffix to decide the field type
        // For now, all values are stored internally as a strings
        String solrName = name + "_s";
        String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(solrName, stringValue));
    }

    @Override public RiakCommandStatement createDMLStatement() {
        if (kindName == null)
            throw InvalidNameException.kind(null);

        String bucketType = provider.settings.bucketType();
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
        // TODO: should the key be inferred instead?

        RiakObject riakObject = new RiakObject()
            .setContentType("application/json")
            .setValue(BinaryValue.create(propertiesJson));
        StoreValue command = new StoreValue.Builder(riakObject)
            .withNamespace(new Namespace(bucketType, kindName))
            .build();

        String shellCommand = String.format(
            """
                curl -X PUT \\
                    -H 'Content-Type: application/json' \\
                    -d '%s' \\
                    %s/types/%s/buckets/%s/keys""",
            propertiesJson,
            provider.settings.createHttpConnectionString(),
            RiakUtils.encodeUriPathSegment(bucketType, StandardCharsets.UTF_8),
            RiakUtils.encodeUriPathSegment(kindName, StandardCharsets.UTF_8));

        return new RiakCommandStatement(shellCommand, command);
    }
}
