package cz.matfyz.tests.example.tpch;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

public abstract class MongoDB {

    private MongoDB() {}

    public static final Datasource datasource = new Datasource(DatasourceType.mongodb, "mongodb");

    // There are no kinds so far but they might be added during the adaptation.

}
