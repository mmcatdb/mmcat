package cz.matfyz.tests.example.adminer;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class Neo4j {

    private Neo4j() {}

    public static final Datasource datasource = new Datasource(DatasourceType.neo4j, "neo4j");

    public static final String userKind = "User";
    public static final String friendKind = "FRIEND";

    public static TestMapping user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.user,
            userKind,
            b -> b.root(
                b.simple("id", Schema.userToUserId)
            )
        );
    }

    public static TestMapping friend(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.friend,
            friendKind,
            b -> b.root(
                b.simple("since", Schema.friendToSince),
                b.complex("_from.User", Schema.friendToFromUser,
                    b.simple("id", Schema.userToUserId)
                ),
                b.complex("_to.User", Schema.friendToToUser,
                    b.simple("id", Schema.userToUserId)
                )
            )
        );
    }

}
