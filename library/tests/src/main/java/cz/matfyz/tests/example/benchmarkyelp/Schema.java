package cz.matfyz.tests.example.benchmarkyelp;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
import cz.matfyz.core.schema.SchemaCategory;

public abstract class Schema {

    public static final String schemaLabel = "Benchmark Yelp Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    public static final BuilderObjex business =            builder.objex("business");
    public static final BuilderObjex businessId =          builder.objex("businessId");
    public static final BuilderObjex businessName =        builder.objex("businessName");
    public static final BuilderObjex businessCity =        builder.objex("businessCity");
    public static final BuilderObjex businessState =       builder.objex("businessState");
    public static final BuilderObjex businessStars =       builder.objex("businessStars");
    public static final BuilderObjex businessReviewCount = builder.objex("businessReviewCount");
    public static final BuilderObjex businessIsOpen =      builder.objex("businessIsOpen");
    public static final BuilderObjex businessCategory =    builder.objex("businessCategory");

    public static final BuilderObjex user =                builder.objex("user");
    public static final BuilderObjex userId =              builder.objex("userId");
    public static final BuilderObjex userName =            builder.objex("userName");
    public static final BuilderObjex userReviewCount =     builder.objex("userReviewCount");
    public static final BuilderObjex userYelpingSince =    builder.objex("userYelpingSince");
    public static final BuilderObjex userUseful =          builder.objex("userUseful");
    public static final BuilderObjex userFunny =           builder.objex("userFunny");
    public static final BuilderObjex userCool =            builder.objex("userCool");

    public static final BuilderObjex review =              builder.objex("review");
    public static final BuilderObjex reviewId =            builder.objex("reviewId");
    public static final BuilderObjex reviewStars =         builder.objex("reviewStars");
    public static final BuilderObjex reviewDate =          builder.objex("reviewDate");
    public static final BuilderObjex reviewUseful =        builder.objex("reviewUseful");
    public static final BuilderObjex reviewFunny =         builder.objex("reviewFunny");
    public static final BuilderObjex reviewCool =          builder.objex("reviewCool");

    // Morphisms

    public static final BuilderMorphism businessToId =      builder.morphism(business, businessId); // 1
    public static final BuilderMorphism businessToName =    builder.morphism(business, businessName);
    public static final BuilderMorphism businessToCity =    builder.morphism(business, businessCity);
    public static final BuilderMorphism businessToState =   builder.morphism(business, businessState);
    public static final BuilderMorphism businessToStars =   builder.morphism(business, businessStars);
    public static final BuilderMorphism businessToRevCnt =  builder.morphism(business, businessReviewCount);
    public static final BuilderMorphism businessToIsOpen =  builder.morphism(business, businessIsOpen);
    public static final BuilderMorphism businessToCtgry =   builder.morphism(businessCategory, business); // not used for now

    public static final BuilderMorphism userToId =          builder.morphism(user, userId); // 9
    public static final BuilderMorphism userToName =        builder.morphism(user, userName);
    public static final BuilderMorphism userToReviewCount = builder.morphism(user, userReviewCount);
    public static final BuilderMorphism userToYelpingSince = builder.morphism(user, userYelpingSince);
    public static final BuilderMorphism userToUseful =      builder.morphism(user, userUseful);
    public static final BuilderMorphism userToFunny =       builder.morphism(user, userFunny);
    public static final BuilderMorphism userToCool =        builder.morphism(user, userCool);
    public static final BuilderMorphism userToFriend =      builder.morphism(user, user); // TODO: maybe do double morphism with #role tag?

    public static final BuilderMorphism reviewToId =        builder.morphism(review, reviewId); // 17
    public static final BuilderMorphism reviewToUser =      builder.morphism(review, user);
    public static final BuilderMorphism reviewToBusiness =  builder.morphism(review, business);
    public static final BuilderMorphism reviewToStars =     builder.morphism(review, reviewStars);
    public static final BuilderMorphism reviewToDate =      builder.morphism(review, reviewDate);
    public static final BuilderMorphism reviewToUseful =    builder.morphism(review, reviewUseful);
    public static final BuilderMorphism reviewToFunny =     builder.morphism(review, reviewFunny);
    public static final BuilderMorphism reviewToCool =      builder.morphism(review, reviewCool);

    // Ids

    static {

        builder
            .ids(business, businessToId)
            .ids(review, reviewToId)
            .ids(user, userToId);

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
