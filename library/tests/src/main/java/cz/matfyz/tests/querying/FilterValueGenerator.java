package cz.matfyz.tests.querying;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.LeafResult;
import cz.matfyz.core.querying.MapResult;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.QueryToInstance;
import cz.matfyz.tests.example.common.TestDatasource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

// TODO: Create an extending class specifically for cal.com with some extra stuff

public class FilterValueGenerator {
    protected final HashMap<Signature, Values> values;

    final SchemaCategory schema;
    final List<TestDatasource<?>> datasources;

    static final Random random = new Random(1234); // we need a seedable generator for reproducible results

    static final int TARGET_VALUES_SIZE = 1000;

    protected static abstract class Values {
        public Values() {}
        public abstract String get();
    }
    protected static class StringValues extends Values {
        private final List<String> values;

        public StringValues(List<String> values) {
            if (values.size() == 0) throw new RuntimeException("Values list is empty (registering value for FilterQueryFiller)");
            this.values = values;
        }

        public StringValues(String... values) {
            this(List.of(values));
        }

        @Override
        public String get() {
            return values.get(random.nextInt(values.size()));
        }
    }
    protected static class DateValues extends Values {
        // minDate, maxDate
        private final long min, max;

        public DateValues(Date minDate, Date maxDate) {
            min = minDate.getTime(); max = maxDate.getTime();
        }

        @Override
        public String get() {
            var d = new Date(random.nextLong(min, max));

            final var format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.format(d);
        }
    }
    protected static class IntValues extends Values {
        // minDate, maxDate
        private final int min, max;

        public IntValues(int min, int max) {
            this.min = min; this.max = max;
        }

        @Override
        public String get() {
            return Integer.toString(random.nextInt(min, max));
        }
    }

    public FilterValueGenerator(SchemaCategory schema, List<TestDatasource<?>> datasources) {
        this.values = new HashMap<>();

        this.schema = schema;
        this.datasources = datasources;
    }

    public String generateValue(Signature signature) {
        final var bSignature = signature.getLast();
        final var generator = getValuesForSignature(bSignature);

        return generator.get();
        // TODO: put out a warning if a value is used twice (i.e. use values in sequential order)
    }

    private Values getValuesForSignature(BaseSignature signature) {
        if (values.containsKey(signature)) return values.get(signature);

        final String query = "SELECT { ?o v ?v . } WHERE { ?o " + signature.toString() + " ?v . }";

        final var provider = new DefaultControlWrapperProvider();
        final var kinds = defineKinds(provider);
        final var queryToInstance = new QueryToInstance(provider, schema, query, kinds, null);

        final var r = queryToInstance.execute();

        final var resultSet = new HashSet<String>();
        for (final var node : r.result().children()) {
            final var a = ((LeafResult)((MapResult)node).children().get("v")).value;
            resultSet.add(a);
        }

        var result = resultSet.stream().toList();
        if (result.size() <= TARGET_VALUES_SIZE) {
            final var newValues = new StringValues(result);
            values.put(signature, newValues);
            return newValues;
        }

        // If result is bigger, reduce it to a random subset
        final HashSet<String> allValues = new HashSet<>(result);
        if (allValues.size() <= TARGET_VALUES_SIZE) {
            result = allValues.stream().toList();
        } else {
            HashSet<String> values = new HashSet<>();
            while (values.size() < TARGET_VALUES_SIZE) {
                values.add(result.get(random.nextInt(result.size())));
            }
            result = values.stream().toList();
        }

        final var newValues = new StringValues(result);
        values.put(signature, newValues);
        return newValues;
    }

    public void ensureValuesForSignature(Signature signature) {
        getValuesForSignature(signature.getLast());
    }

    private List<Mapping> defineKinds(DefaultControlWrapperProvider provider) {
        return datasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            }).toList();
    }
}
