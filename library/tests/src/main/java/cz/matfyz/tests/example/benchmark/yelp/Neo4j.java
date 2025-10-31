package cz.matfyz.tests.example.benchmark.yelp;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class Neo4j {

    private Neo4j() {}

    public static final Datasource datasource = new Datasource(DatasourceType.neo4j, "neo4j");

    public static final String businessKind = "business";
    public static final String userKind = "user";
    public static final String reviewKind = "REVIEW";
    public static final String friendshipKind = "FRIENDSHIP";

    public static TestMapping business(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.business,
            businessKind,
            b -> b.root(
                b.simple("business_id", Schema.business_id),
                b.simple("name", Schema.business_name),
                b.simple("city", Schema.business_city),
                b.simple("state", Schema.business_state),
                // b.simple("stars", Schema.business_stars),
                // b.simple("review_count", Schema.business_revCnt),
                b.simple("is_open", Schema.business_isOpen)
            )
        );
    }

    public static TestMapping user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.user,
            userKind,
            b -> b.root(
                b.simple("user_id", Schema.user_id),
                b.simple("name", Schema.user_name),
                // b.simple("review_count", Schema.user_reviewCount),
                b.simple("yelping_since", Schema.user_yelpingSince)
                // b.simple("useful", Schema.user_useful),
                // b.simple("funny", Schema.user_funny),
                // b.simple("cool", Schema.user_cool)
            )
        );
    }

    // TODO: check that this works
    public static TestMapping friendship(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.friendship,
            friendshipKind,
            b -> b.root(
                b.complex("_from.User", Schema.friendship_user1,
                    b.simple("user_id", Schema.user_id)
                ),
                b.complex("_to.User", Schema.friendship_user2,
                    b.simple("user_id", Schema.user_id)
                )
            )
        );
    }

    public static TestMapping review(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.review,
            reviewKind,
            b -> b.root(
                b.simple("review_id", Schema.review_id),

                b.complex("_to.User", Schema.review_user,
                    b.simple("user_id", Schema.user_id)
                ),
                b.complex("_from.Business", Schema.review_business,
                    b.simple("business_id", Schema.business_id)
                ),

                b.simple("stars", Schema.review_stars),
                b.simple("date", Schema.review_date),
                b.simple("useful", Schema.review_useful),
                b.simple("funny", Schema.review_funny),
                b.simple("cool", Schema.review_cool)
            )
        );
    }

}
