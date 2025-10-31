package cz.matfyz.tests.example.benchmark.yelp;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Tag;

public abstract class Schema {

    public static final String schemaLabel = "Benchmark Yelp Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    public static final BuilderObjex business =             builder.objex("business");
    public static final BuilderObjex businessId =           builder.objex("businessId");
    public static final BuilderObjex businessName =         builder.objex("businessName");
    public static final BuilderObjex businessCity =         builder.objex("businessCity");
    public static final BuilderObjex businessState =        builder.objex("businessState");
    // public static final BuilderObjex businessStars =        builder.objex("businessStars");
    // public static final BuilderObjex businessReviewCount =  builder.objex("businessReviewCount");
    public static final BuilderObjex businessIsOpen =       builder.objex("businessIsOpen");
    // public static final BuilderObjex businessCategory =     builder.objex("businessCategory");

    public static final BuilderObjex user =                 builder.objex("user");
    public static final BuilderObjex userId =               builder.objex("userId");
    public static final BuilderObjex userName =             builder.objex("userName");
    // public static final BuilderObjex userReviewCount =      builder.objex("userReviewCount");
    public static final BuilderObjex userYelpingSince =     builder.objex("userYelpingSince");
    // public static final BuilderObjex userUseful =           builder.objex("userUseful");
    // public static final BuilderObjex userFunny =            builder.objex("userFunny");
    // public static final BuilderObjex userCool =             builder.objex("userCool");
    public static final BuilderObjex friendship =           builder.objex("friendship");

    public static final BuilderObjex review =               builder.objex("review");
    public static final BuilderObjex reviewId =             builder.objex("reviewId");
    public static final BuilderObjex reviewStars =          builder.objex("reviewStars");
    public static final BuilderObjex reviewDate =           builder.objex("reviewDate");
    public static final BuilderObjex reviewUseful =         builder.objex("reviewUseful");
    public static final BuilderObjex reviewFunny =          builder.objex("reviewFunny");
    public static final BuilderObjex reviewCool =           builder.objex("reviewCool");

    // Morphisms

    public static final BuilderMorphism business_id =      builder.morphism(business, businessId); // 1
    public static final BuilderMorphism business_name =    builder.morphism(business, businessName);
    public static final BuilderMorphism business_city =    builder.morphism(business, businessCity);
    public static final BuilderMorphism business_state =   builder.morphism(business, businessState);
    // public static final BuilderMorphism business_stars =   builder.morphism(business, businessStars);
    // public static final BuilderMorphism business_revCnt =  builder.morphism(business, businessReviewCount);
    public static final BuilderMorphism business_isOpen =  builder.morphism(business, businessIsOpen);
    // public static final BuilderMorphism business_ctgry =   builder.morphism(businessCategory, business); // not used for now

    public static final BuilderMorphism user_id =          builder.morphism(user, userId); // 6
    public static final BuilderMorphism user_name =        builder.morphism(user, userName);
    // public static final BuilderMorphism user_reviewCount = builder.morphism(user, userReviewCount);
    public static final BuilderMorphism user_yelpingSince = builder.morphism(user, userYelpingSince);
    // public static final BuilderMorphism user_useful =      builder.morphism(user, userUseful);
    // public static final BuilderMorphism user_funny =       builder.morphism(user, userFunny);
    // public static final BuilderMorphism user_cool =        builder.morphism(user, userCool);

    public static final BuilderMorphism friendship_user1 = builder.tags(Tag.role).morphism(friendship, user); // 9
    public static final BuilderMorphism friendship_user2 = builder.tags(Tag.role).morphism(friendship, user);

    public static final BuilderMorphism review_id =        builder.morphism(review, reviewId); // 11
    public static final BuilderMorphism review_user =      builder.morphism(review, user);
    public static final BuilderMorphism review_business =  builder.morphism(review, business);
    public static final BuilderMorphism review_stars =     builder.morphism(review, reviewStars);
    public static final BuilderMorphism review_date =      builder.morphism(review, reviewDate);
    public static final BuilderMorphism review_useful =    builder.morphism(review, reviewUseful);
    public static final BuilderMorphism review_funny =     builder.morphism(review, reviewFunny);
    public static final BuilderMorphism review_cool =      builder.morphism(review, reviewCool);

    // Ids

    static {
        builder
            .ids(business, business_id)
            .ids(review, review_id)
            .ids(user, user_id)
            .ids(friendship, friendship_user1, friendship_user2);
    }

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchema() {
        return builder.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return builder.buildMetadata(schema);
    }

    private Schema() {}

}
