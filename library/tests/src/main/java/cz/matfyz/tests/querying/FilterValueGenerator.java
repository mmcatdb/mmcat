package cz.matfyz.tests.querying;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
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
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

// TODO: Create an extending class specifically for cal.com with some extra stuff

public class FilterValueGenerator {
    final HashMap<Signature, Values> values;

    final SchemaCategory schema;
    final List<TestDatasource<?>> datasources;

    protected static abstract class Values {
        public Values() {}
        public abstract String get();
    }
    protected static class StringValues extends Values {
        private final List<String> values;
        private int index;

        public StringValues(List<String> values) {
            if (values.size() == 0) throw new RuntimeException("Values list is empty (registering value for FilterQueryFiller)");
            this.values = values;
            this.index = 0;
        }

        @Override
        public String get() {
            index = (index + 1) % values.size();
            return values.get(index);
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
            var d = new Date(ThreadLocalRandom.current().nextLong(min, max));

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
            return Integer.toString(ThreadLocalRandom.current().nextInt(min, max));
        }
    }

    public FilterValueGenerator(SchemaCategory schema, List<TestDatasource<?>> datasources) {
        this.values = new HashMap<>();

        this.schema = schema;
        this.datasources = datasources;
    }

    public String generateValue(Signature signature) {
        signature = signature.getLast();
        final var generator = values.get(signature);
        if (generator == null) return null;
        return generator.get();
        // TODO: put out a warning if a value is used twice (i.e. use values in sequential order)
    }

    public void registerForQueriedValue(Signature signature) {
        signature = signature.getLast();

        if (values.containsKey(signature)) return;

        final String query = "SELECT { ?o v ?v . } WHERE { ?o " + signature.toString() + " ?v . }";

        final var provider = new DefaultControlWrapperProvider();
        final var kinds = defineKinds(provider);
        final var queryToInstance = new QueryToInstance(provider, schema, query, kinds, null);

        final var r = queryToInstance.execute();
        final var result = r
            .children().stream().map(node -> ((MapResult)node).children().get("v"))
            .map(node -> ((LeafResult)node).value).toList();

        // TODO: reduce the result into, maybe (parametrize), 1000 items selected randomly

        values.put(signature, new StringValues(result));
    }

    private List<Mapping> defineKinds(DefaultControlWrapperProvider provider) {
        return datasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            }).toList();
    }
}
