package cz.matfyz.tests.example.benchmarkyelp;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObject;

public abstract class Schema {

    public static final String schemaLabel = "Benchmark Yelp Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    public static final BuilderObject business =            builder.object("business");
    public static final BuilderObject businessId =          builder.object("businessId");
    public static final BuilderObject businessName =        builder.object("businessName");
    public static final BuilderObject businessCity =        builder.object("businessCity");
    public static final BuilderObject businessState =       builder.object("businessState");
    public static final BuilderObject businessStars =       builder.object("businessStars");
    public static final BuilderObject businessReviewCount = builder.object("businessReviewCount");
    public static final BuilderObject businessIsOpen =      builder.object("businessIsOpen");
    public static final BuilderObject businessCategory =    builder.object("businessCategory");

    public static final BuilderObject user =                builder.object("user");
    public static final BuilderObject userId =              builder.object("userId");
    public static final BuilderObject userName =            builder.object("userName");
    public static final BuilderObject userReviewCount =     builder.object("userReviewCount");
    public static final BuilderObject userYelpingSince =    builder.object("userYelpingSince");
    public static final BuilderObject userUseful =          builder.object("userUseful");
    public static final BuilderObject userFunny =           builder.object("userFunny");
    public static final BuilderObject userCool =            builder.object("userCool");

    public static final BuilderObject review =              builder.object("review");
    public static final BuilderObject reviewId =            builder.object("reviewId");
    public static final BuilderObject reviewStars =         builder.object("reviewStars");
    public static final BuilderObject reviewDate =          builder.object("reviewDate");
    public static final BuilderObject reviewUseful =        builder.object("reviewUseful");
    public static final BuilderObject reviewFunny =         builder.object("reviewFunny");
    public static final BuilderObject reviewCool =          builder.object("reviewCool");

    // Morphisms

    public static final BuilderMorphism businessToId =      builder.morphism(business, businessId);
    public static final BuilderMorphism businessToName =    builder.morphism(business, businessName);
    public static final BuilderMorphism businessToCity =    builder.morphism(business, businessCity);
    public static final BuilderMorphism businessToState =   builder.morphism(business, businessState);
    public static final BuilderMorphism businessToStars =   builder.morphism(business, businessStars);
    public static final BuilderMorphism businessToRevCnt =  builder.morphism(business, businessReviewCount);
    public static final BuilderMorphism businessToIsOpen =  builder.morphism(business, businessIsOpen);
    public static final BuilderMorphism businessToCtgry =   builder.morphism(businessCategory, business);

    public static final BuilderMorphism userToId =          builder.morphism(user, userId);
    public static final BuilderMorphism userToName =        builder.morphism(user, userName);
    public static final BuilderMorphism userToReviewCount = builder.morphism(user, userReviewCount);
    public static final BuilderMorphism userToYelpingSince = builder.morphism(user, userYelpingSince);
    public static final BuilderMorphism userToUseful =      builder.morphism(user, userUseful);
    public static final BuilderMorphism userToFunny =       builder.morphism(user, userFunny);
    public static final BuilderMorphism userToCool =        builder.morphism(user, userCool);
    public static final BuilderMorphism userToFriend =      builder.morphism(user, user); // TODO: maybe do double morphism with #role tag?

    public static final BuilderMorphism reviewToId =        builder.morphism(review, reviewId);
    public static final BuilderMorphism reviewToUser =      builder.morphism(review, user);
    public static final BuilderMorphism reviewToBusiness =  builder.morphism(review, business);
    public static final BuilderMorphism reviewToStars =     builder.morphism(review, reviewStars);
    public static final BuilderMorphism reviewToDate =      builder.morphism(review, reviewDate);
    public static final BuilderMorphism reviewToUseful =    builder.morphism(review, reviewUseful);
    public static final BuilderMorphism reviewToFunny =     builder.morphism(review, reviewFunny);
    public static final BuilderMorphism reviewToCool =      builder.morphism(review, reviewCool);

    /** TODO: maybe create helper script for installation (ask Alzbeta), perhaps one that installs (downloads) docker into data/ directory (its in gitignore) and installs it into Mongo
     * then mention the script somewhere, maybe in library/querying/README??
     */
    /** TODO: modify entrypoint.sh to see which databases are created ... maybe? */

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
