package cz.matfyz.tests.example.adminer;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class MongoDB {

    private MongoDB() {}

    public static final Datasource datasource = new Datasource(DatasourceType.mongodb, "mongodb");

    public static final String businessKind = "business";

    public static TestMapping business(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.business,
            businessKind,
            b -> b.root(
                b.simple("business_id", Schema.business_businessId),
                b.simple("name", Schema.business_name),
                b.simple("city", Schema.business_city),
                b.simple("state", Schema.business_state),
                b.simple("dates", Schema.business_dates),
                b.complex("attributes", Schema.business_attributes,
                    b.simple("wifi", Schema.attributes_wifi),
                    b.simple("outdoor_seating", Schema.attributes_outdoorSeating)
                )
            )
        );
    }
}
