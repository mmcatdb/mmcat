package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.tests.querying.FilterValueGenerator;

import java.util.GregorianCalendar;
import java.util.List;

public class ValueGenerator extends FilterValueGenerator {
    public ValueGenerator(SchemaCategory schema, List<TestDatasource<?>> datasources) {
        super(schema, datasources);

        final var d1 = new GregorianCalendar(2020, 1, 1).getTime();
        final var d2 = new GregorianCalendar(2020, 12, 31).getTime();
        final var dateGen = new DateValues(d1, d2);

        values.put(Schema.availability_start.signature(), dateGen);
        values.put(Schema.availability_end.signature(), dateGen);
        values.put(Schema.outOfOffice_start.signature(), dateGen);
        values.put(Schema.outOfOffice_end.signature(), dateGen);
        values.put(Schema.booking_time.signature(), dateGen);
    }
}
