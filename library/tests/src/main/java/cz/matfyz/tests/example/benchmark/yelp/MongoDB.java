package cz.matfyz.tests.example.benchmark.yelp;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class MongoDB {

    private MongoDB() {}

    public static final Datasource datasource = new Datasource(DatasourceType.mongodb, "mongodb");

    public static final String businessKind = "business";
    public static final String userKind = "yelp_user";
    public static final String reviewKind = "review";

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
                // b.simple("categories", Schema.businessToCtgry)
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
                // b.simple("cool", Schema.userToCool),
                // b.simple("friends", Schema.friendshipToUser1.dual().concatenate(Schema.friendshipToUser2.signature()).concatenate(Schema.userToId.signature()))
            )
        );
    }

    public static TestMapping review(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.review,
            reviewKind,
            b -> b.root(
                b.simple("review_id", Schema.reviewToId),
                b.simple("user_id", Schema.reviewToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("business_id", Schema.reviewToBusiness.signature().concatenate(Schema.businessToId.signature())),
                b.simple("stars", Schema.reviewToStars),
                b.simple("date", Schema.reviewToDate),
                b.simple("useful", Schema.reviewToUseful),
                b.simple("funny", Schema.reviewToFunny),
                b.simple("cool", Schema.reviewToCool)
            )
        );
    }

}
