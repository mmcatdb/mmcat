package cz.matfyz.tests.example.benchmarkyelp;

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
                b.simple("business_id", Schema.businessToId),
                b.simple("name", Schema.businessToName),
                b.simple("city", Schema.businessToCity),
                b.simple("state", Schema.businessToState),
                // b.simple("stars", Schema.businessToStars),
                // b.simple("review_count", Schema.businessToRevCnt),
                b.simple("is_open", Schema.businessToIsOpen)
            )
        );
    }

    public static TestMapping user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.user,
            userKind,
            b -> b.root(
                b.simple("user_id", Schema.userToId),
                b.simple("name", Schema.userToName),
                // b.simple("review_count", Schema.userToReviewCount),
                b.simple("yelping_since", Schema.userToYelpingSince)
                // b.simple("useful", Schema.userToUseful),
                // b.simple("funny", Schema.userToFunny),
                // b.simple("cool", Schema.userToCool)
            )
        );
    }

    // TODO: check that this works
    public static TestMapping friendship(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.friendship,
            friendshipKind,
            b -> b.root(
                b.complex("_from.User", Schema.friendshipToUser1,
                    b.simple("user_id", Schema.userToId)
                ),
                b.complex("_to.User", Schema.friendshipToUser2,
                    b.simple("user_id", Schema.userToId)
                )
            )
        );
    }

    public static TestMapping review(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.review,
            reviewKind,
            b -> b.root(
                b.simple("review_id", Schema.reviewToId),

                b.complex("_to.User", Schema.reviewToUser,
                    b.simple("user_id", Schema.userToId)
                ),
                b.complex("_from.Business", Schema.reviewToBusiness,
                    b.simple("business_id", Schema.businessToId)
                ),

                b.simple("stars", Schema.reviewToStars),
                b.simple("date", Schema.reviewToDate),
                b.simple("useful", Schema.reviewToUseful),
                b.simple("funny", Schema.reviewToFunny),
                b.simple("cool", Schema.reviewToCool)
            )
        );
    }

}
