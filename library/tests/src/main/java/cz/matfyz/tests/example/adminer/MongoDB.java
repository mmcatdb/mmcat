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
                b.simple("business_id", Schema.businessToBusinessId),
                b.simple("name", Schema.businessToName),
                b.simple("city", Schema.businessToCity),
                b.simple("state", Schema.businessToState),
                b.simple("dates", Schema.businessToDates),
                b.complex("attributes", Schema.businessToAttributes,
                    b.simple("wifi", Schema.attributesToWifi),
                    b.simple("outdoor_seating", Schema.attributesToOutdoorSeating)
                )
            )
        );
    }
}
